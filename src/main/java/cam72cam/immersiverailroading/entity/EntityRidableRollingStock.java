package cam72cam.immersiverailroading.entity;

import cam72cam.immersiverailroading.Config;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.entity.EntityCoupleableRollingStock.CouplerType;
import cam72cam.immersiverailroading.util.bvh.MeshNavigator;
import cam72cam.immersiverailroading.library.Permissions;
import cam72cam.immersiverailroading.model.part.Door;
import cam72cam.immersiverailroading.model.part.Seat;
import cam72cam.immersiverailroading.util.MathUtil;
import cam72cam.immersiverailroading.util.VecUtil;
import cam72cam.mod.entity.Entity;
import cam72cam.mod.entity.Player;
import cam72cam.mod.entity.boundingbox.IBoundingBox;
import cam72cam.mod.entity.custom.IRidable;
import cam72cam.mod.entity.sync.TagSync;
import cam72cam.mod.item.ClickResult;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.model.obj.OBJFace;
import cam72cam.mod.serialization.SerializationException;
import cam72cam.mod.serialization.TagCompound;
import cam72cam.mod.serialization.TagField;
import cam72cam.mod.serialization.TagMapper;

import java.util.*;
import java.util.stream.Collectors;

public abstract class EntityRidableRollingStock extends EntityBuildableRollingStock implements IRidable {
	@TagField(value = "payingPassengerPositions", mapper = PassengerMapper.class)
	private Map<UUID, Vec3d> payingPassengerPositions = new HashMap<>();

	@TagField(value = "seatedPassengers", mapper = SeatedMapper.class)
	@TagSync
	private Map<String, UUID> seatedPassengers = new HashMap<>();

	// Hack to remount players if they were seated
	private Map<UUID, Vec3d> remount = new HashMap<>();

	public boolean useCustomMovementData;

	public float getRidingSoundModifier() {
		return getDefinition().dampeningAmount;
	}

	@Override
	public void load(TagCompound tag) {
		this.useCustomMovementData = getDefinition().navigator.hasNavMesh();
	}

	@Override
	public ClickResult onClick(Player player, Player.Hand hand) {
		ClickResult clickRes = super.onClick(player, hand);
		if (clickRes != ClickResult.PASS) {
			return clickRes;
		}

		if (player.isCrouching()) {
			return ClickResult.PASS;
		} else if (isPassenger(player)) {
			return ClickResult.PASS;
		} else {
			if (getWorld().isServer) {
				player.startRiding(this);
			}
			return ClickResult.ACCEPTED;
		}
	}

	protected Vec3d getSeatPosition(UUID passenger) {
		String seat = seatedPassengers.entrySet().stream()
				.filter(x -> x.getValue().equals(passenger))
				.map(Map.Entry::getKey).findFirst().orElse(null);
		return this.getDefinition().getModel().getSeats().stream()
				.filter(s -> s.part.key.equals(seat))
				.map(s -> new Vec3d(s.part.center.z, s.part.min.y, -s.part.center.x).scale(gauge.scale()).subtract(0, 0.6, 0))
				.findFirst().orElse(null);
	}

	@Override
	public Vec3d getMountOffset(Entity passenger, Vec3d off) {
		if (useCustomMovementData) {
			Vec3d seat = getSeatPosition(passenger.getUUID());
			if (seat != null) {
				return seat;
			}

			MeshNavigator navMesh = this.getDefinition().navigator;
			double scale = gauge.scale();
			off = off.scale(scale);

			Vec3d realOffset = off.rotateYaw(-90);

			double bbOffset = 4 * scale; //4 meters in original model
			IBoundingBox range = IBoundingBox.from(
					realOffset.subtract(bbOffset, bbOffset, bbOffset),
					realOffset.add(bbOffset, bbOffset, bbOffset)
			);

			List<OBJFace> nearby = navMesh.getFloorFacesWithin(range, scale);

			return nearby.stream()
						 .map(tri -> MathUtil.closestPointOnTriangle(realOffset, tri))
						 .min(Comparator.comparingDouble(point -> realOffset.subtract(point).lengthSquared()))
						 .map(closestPoint -> closestPoint.rotateYaw(90))
						 .orElse(null);
		}

		if (passenger.isVillager() && !payingPassengerPositions.containsKey(passenger.getUUID())) {
			payingPassengerPositions.put(passenger.getUUID(), passenger.getPosition());
		}

		if (passenger.isVillager() && !seatedPassengers.containsValue(passenger.getUUID())) {
			for (Seat<?> seat : getDefinition().getModel().getSeats()) {
				if (!seatedPassengers.containsKey(seat.part.key)) {
					seatedPassengers.put(seat.part.key, passenger.getUUID());
					break;
				}
			}
		}

		Vec3d seat = getSeatPosition(passenger.getUUID());
		if (seat != null) {
			return seat;
		}

		int wiggle = passenger.isVillager() ? 10 : 0;
		off = off.add((Math.random()-0.5) * wiggle, 0, (Math.random()-0.5) * wiggle);
		off = this.getDefinition().correctPassengerBounds(gauge, off, shouldRiderSit(passenger));

		return off;
	}

	@Override
	public boolean canFitPassenger(Entity passenger) {
		if (passenger instanceof Player && !((Player) passenger).hasPermission(Permissions.BOARD_STOCK)) {
			return false;
		}
		return getPassengerCount() < this.getDefinition().getMaxPassengers();
	}
	
	@Override
	public boolean shouldRiderSit(Entity passenger) {
		boolean nonSeated = this.getDefinition().shouldSit != null ? this.getDefinition().shouldSit : this.gauge.shouldSit();
		return nonSeated || this.seatedPassengers.containsValue(passenger.getUUID());
	}

	@Override
	public Vec3d onPassengerUpdate(Entity passenger, Vec3d offset) {
		if (useCustomMovementData) {
			Vec3d seat = getSeatPosition(passenger.getUUID());
			if (seat != null) {
				return seat;
			}

			Vec3d movement = Vec3d.ZERO;
			if (passenger.isPlayer()) {
				movement = movement(passenger.asPlayer(), offset);
			}
			Vec3d targetXZ = VecUtil.rotatePitch(movement, -this.getRotationPitch());

			Vec3d rayStart = targetXZ.rotateYaw(-90).add(0, 1, 0);
			Vec3d rayDir = new Vec3d(0, -1, 0);

			Vec3d localTarget = targetXZ.rotateYaw(-90);

			double scale = this.gauge.scale();
			double bbOffset = 0.5 * scale;
			IBoundingBox rayBox = IBoundingBox.from(
					localTarget.subtract(bbOffset, bbOffset, bbOffset),
					localTarget.add(bbOffset, bbOffset, bbOffset)
			);
			MeshNavigator navMesh = getDefinition().navigator;
			List<OBJFace> nearby = navMesh.getFloorFacesWithin(rayBox, scale);

			OptionalDouble maxY = nearby.stream()
										.map(tri -> MathUtil.intersectRayTriangle(rayStart, rayDir, tri))
										.filter(t -> t != null && t >= 0)
										.mapToDouble(t -> rayStart.add(rayDir.scale(t)).y)
										.max();

			if (maxY.isPresent()) {
				offset = VecUtil.rotatePitch(new Vec3d(targetXZ.x, maxY.getAsDouble(), targetXZ.z), this.getRotationPitch());
			}
		} else {
			if (passenger.isPlayer()) {
				offset = playerMovement(passenger.asPlayer(), offset);
			}

			Vec3d seat = getSeatPosition(passenger.getUUID());
			if (seat != null) {
				offset = seat;
			} else {
				offset = this.getDefinition().correctPassengerBounds(gauge, offset, shouldRiderSit(passenger));
			}
			offset = offset.add(0, Math.sin(Math.toRadians(this.getRotationPitch())) * offset.z, 0);
		}
		return offset;
	}

	protected boolean isNearestConnectingDoorOpen(Player source) {
		// Find any doors that are close enough that are closed (and then negate)
		return !this.getDefinition().getModel().getDoors().stream()
				.filter(d -> d.type == Door.Types.CONNECTING)
				.filter(d -> d.center(this).distanceTo(source.getPosition()) < getDefinition().getLength(this.gauge)/3)
				.min(Comparator.comparingDouble(d -> d.center(this).distanceTo(source.getPosition())))
				.filter(x -> !x.isOpen(this))
				.isPresent();
	}

	//Used for custom movement
	private boolean isInternalDoorOpen(Vec3d start, Vec3d end) {
		start = VecUtil.rotatePitch(start, this.getRotationPitch());
		end  = VecUtil.rotatePitch(end, -this.getRotationPitch());

		Vec3d finalStart = start.rotateYaw(-90);
		Vec3d finalEnd = start.add(end.rotateYaw(-90));

		return getDefinition().getModel().getDoors().stream()
							  .filter(d -> d.type == Door.Types.INTERNAL || d.type == Door.Types.CONNECTING)
							  .filter(d -> !d.isOpen(this))
							  .map(door -> IBoundingBox.from(door.part.min, door.part.max))
							  .anyMatch(box -> box.intersectsSegment(finalStart, finalEnd));
	}

	protected Vec3d playerMovement(Player source, Vec3d offset) {
		Vec3d movement = source.getMovementInput();
        /*
        if (sprinting) {
            movement = movement.scale(3);
        }
        */
        if (movement.length() < 0.1) {
            return offset;
        }

        movement = new Vec3d(movement.x, 0, movement.z).rotateYaw(this.getRotationYaw() - source.getRotationYawHead());

        offset = offset.add(movement);

        if (this instanceof EntityCoupleableRollingStock) {
			EntityCoupleableRollingStock couplable = (EntityCoupleableRollingStock) this;

			boolean atFront = this.getDefinition().isAtFront(gauge, offset);
			boolean atBack = this.getDefinition().isAtRear(gauge, offset);
			// TODO config for strict doors
			boolean atDoor = isNearestConnectingDoorOpen(source);

			atFront &= atDoor;
			atBack &= atDoor;

			for (CouplerType coupler : CouplerType.values()) {
				boolean atCoupler = coupler == CouplerType.FRONT ? atFront : atBack;
				if (atCoupler && couplable.isCoupled(coupler)) {
					EntityCoupleableRollingStock coupled = ((EntityCoupleableRollingStock) this).getCoupled(coupler);
					if (coupled != null) {
						if (coupled.isNearestConnectingDoorOpen(source)) {
							coupled.addPassenger(source);
						}
					} else if (this.getTickCount() > 20) {
						ImmersiveRailroading.info(
								"Tried to move between cars (%s, %s), but %s was not found",
								this.getUUID(),
								couplable.getCoupledUUID(coupler),
								couplable.getCoupledUUID(coupler)
						);
					}
					return offset;
				}
			}
        }

        if (getDefinition().getModel().getDoors().stream()
						   .anyMatch(x -> x.isAtOpenDoor(source, this, Door.Types.EXTERNAL))
				&& getWorld().isServer
				&& !this.getDefinition().correctPassengerBounds(gauge, offset, shouldRiderSit(source)).equals(offset)
		) {
        	this.removePassenger(source);
		}

		return offset;
	}

	@Override
	public void onTick() {
		super.onTick();

		if (getWorld().isServer) {
			remount.forEach((uuid, pos) -> {
				Player player = getWorld().getEntity(uuid, Player.class);
				if (player != null) {
					player.setPosition(pos);
					player.startRiding(this);
				}
			});
			remount.clear();
			for (Player source : getWorld().getEntities(Player.class)) {
				if (source.getRiding() == null && getDefinition().getModel().getDoors().stream().anyMatch(x -> x.isAtOpenDoor(source, this, Door.Types.EXTERNAL))) {
					this.addPassenger(source);
				}
			}
		}
	}

	public Vec3d onDismountPassenger(Entity passenger, Vec3d offset) {
		List<String> seats = seatedPassengers.entrySet().stream().filter(x -> x.getValue().equals(passenger.getUUID()))
				.map(Map.Entry::getKey).collect(Collectors.toList());
		if (!seats.isEmpty()) {
			seats.forEach(seatedPassengers::remove);
			if (getWorld().isServer && passenger.isPlayer()) {
				remount.put(passenger.getUUID(), passenger.getPosition());
			}
		}

		//TODO calculate better dismount offset
		offset = new Vec3d(Math.copySign(getDefinition().getWidth(gauge)/2 + 1, offset.x), 0, offset.z);

		if (getWorld().isServer && passenger.isVillager() && payingPassengerPositions.containsKey(passenger.getUUID())) {
			double distanceMoved = passenger.getPosition().distanceTo(payingPassengerPositions.get(passenger.getUUID()));

			int payout = (int) Math.floor(distanceMoved * Config.ConfigBalance.villagerPayoutPerMeter);

			List<ItemStack> payouts = Config.ConfigBalance.getVillagerPayout();
			if (payouts.size() != 0) {
				int type = (int)(Math.random() * 100) % payouts.size();
				ItemStack stack = payouts.get(type).copy();
				stack.setCount(payout);
				getWorld().dropItem(stack, getBlockPosition());
				// TODO drop by player or new pos?
			}
			payingPassengerPositions.remove(passenger.getUUID());
		}

		return offset;
	}

	public Vec3d movement(Player source, Vec3d offset) {
		Vec3d movement = source.getMovementInput();
		if (movement.length() <= 0.1) {
			return offset;
		}

		movement = new Vec3d(movement.x, 0, movement.z).rotateYaw(this.getRotationYaw() - source.getRotationYawHead());
		Vec3d localOffset = offset.rotateYaw(-90).add(movement.rotateYaw(-90));

		IBoundingBox rayBox = IBoundingBox.from(
				localOffset.subtract(0.2f, 0.2f, 0.2f),
				localOffset.add(0.2f, 0.2f, 0.2f)
		);
		MeshNavigator navMesh = getDefinition().navigator;
		List<OBJFace> nearby = navMesh.getCollisionFacesWithin(rayBox, this.gauge.scale());

		Vec3d rayStart = localOffset.add(0, 1, 0);
		Vec3d rayDir = movement.rotateYaw(-90).normalize();

		for (OBJFace tri : nearby) {
			Double t = MathUtil.intersectRayTriangle(rayStart, rayDir, tri);
			if (t != null && t >= 0) {
				return offset;
			}
		}

		if (isInternalDoorOpen(offset, movement)) {
			return offset;
		}

		offset = offset.add(movement);

		if (getWorld().isServer) {
			for (Door<?> door : getDefinition().getModel().getDoors()) {
				if (door.isAtOpenDoor(source, this, Door.Types.EXTERNAL)) {
					Vec3d doorCenter = door.center(this);
					Vec3d toDoor = doorCenter.subtract(offset).normalize();
					double dot = toDoor.dotProduct(movement.normalize());
					if (dot > 0.5) {
						this.removePassenger(source);
						break;
					}
				}
			}
		}

		if (this instanceof EntityCoupleableRollingStock) {
			EntityCoupleableRollingStock coupleable = (EntityCoupleableRollingStock) this;

			boolean isAtFront = isAtCouplerWithFloor(offset, movement, EntityCoupleableRollingStock.CouplerType.FRONT);
			boolean isAtBack =  isAtCouplerWithFloor(offset, movement, EntityCoupleableRollingStock.CouplerType.BACK);
			boolean atDoor = isNearestConnectingDoorOpen(source);

			isAtFront &= atDoor;
			isAtBack &= atDoor;

			for (EntityCoupleableRollingStock.CouplerType coupler : EntityCoupleableRollingStock.CouplerType.values()) {
				boolean atCoupler = coupler == EntityCoupleableRollingStock.CouplerType.FRONT ? isAtFront : isAtBack;
				if (atCoupler && coupleable.isCoupled(coupler)) {
					EntityCoupleableRollingStock coupled = ((EntityCoupleableRollingStock) this).getCoupled(coupler);
					if (coupled != null) {
						if (coupled.isNearestConnectingDoorOpen(source)) {
							coupled.addPassenger(source);
						}
					} else if (this.getTickCount() > 20) {
						ImmersiveRailroading.info(
								"Tried to move between cars (%s, %s), but %s was not found",
								this.getUUID(),
								coupleable.getCoupledUUID(coupler),
								coupleable.getCoupledUUID(coupler)
						);
					}
				}
			}
		}

		return offset;
	}

	private boolean isAtCouplerWithFloor(Vec3d offset, Vec3d movement, EntityCoupleableRollingStock.CouplerType type) {
		double coupler = getDefinition().getCouplerPosition(type, this.gauge);
		Vec3d couplerPos = new Vec3d(type == EntityCoupleableRollingStock.CouplerType.FRONT ? -coupler : coupler, offset.y, offset.z);

		double scale = this.gauge.scale();
		double bbOffset = 0.2 * scale;
		IBoundingBox range = IBoundingBox.from(
				couplerPos.subtract(bbOffset, bbOffset, bbOffset),
				couplerPos.add(bbOffset, bbOffset, bbOffset)
		);

		MeshNavigator navMesh = getDefinition().navigator;
		List<OBJFace> nearby = navMesh.getFloorFacesWithin(range, scale);

		Vec3d finalOffset = offset.rotateYaw(-90);
		return nearby.stream()
					 .map(face -> MathUtil.closestPointOnTriangle(finalOffset, face))
					 .map(point -> finalOffset.subtract(point).length())
					 .filter(dis -> dis < 0.5)
					 .anyMatch(d -> {
						 Vec3d toCoupler = couplerPos.subtract(finalOffset).normalize();
					 	 double dot = toCoupler.dotProduct(movement.rotateYaw(-90).normalize());
					 	 return dot > 0.5;
					 });
	}

	public void onSeatClick(String seat, Player player) {
		List<String> seats = seatedPassengers.entrySet().stream().filter(x -> x.getValue().equals(player.getUUID()))
											 .map(Map.Entry::getKey).collect(Collectors.toList());
		if (!seats.isEmpty()) {
			seats.forEach(seatedPassengers::remove);
			return;
		}

		seatedPassengers.put(seat, player.getUUID());
	}

	private static class PassengerMapper implements TagMapper<Map<UUID, Vec3d>> {
		@Override
		public TagAccessor<Map<UUID, Vec3d>> apply(Class<Map<UUID, Vec3d>> type, String fieldName, TagField tag) {
			return new TagAccessor<>(
					(d, o) -> d.setMap(fieldName, o, UUID::toString, (Vec3d pos) -> new TagCompound().setVec3d("pos", pos)),
					d -> d.getMap(fieldName, UUID::fromString, t -> t.getVec3d("pos"))
			);
		}
	}

	private static class SeatedMapper implements TagMapper<Map<String, UUID>> {
		@Override
		public TagAccessor<Map<String, UUID>> apply(Class<Map<String, UUID>> type, String fieldName, TagField tag) throws SerializationException {
			return new TagAccessor<>(
					(d, o) -> d.setMap(fieldName, o, i -> i, u -> new TagCompound().setUUID("uuid", u)),
					d -> d.getMap(fieldName, i -> i, t -> t.getUUID("uuid"))
			);
		}
	}
}
