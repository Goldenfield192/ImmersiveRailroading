package cam72cam.immersiverailroading.entity;

import cam72cam.immersiverailroading.Config;
import cam72cam.immersiverailroading.ImmersiveRailroading;
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

	public float getRidingSoundModifier() {
		return getDefinition().dampeningAmount;
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
		off = off.add((Math.random() - 0.5) * wiggle, 0, (Math.random() - 0.5) * wiggle);

		off = restrictPassengerPosition(off);

		if(shouldRiderSit(passenger)) {
			off = off.subtract(0, 0.75, 0);
		}

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
		Vec3d seat = getSeatPosition(passenger.getUUID());
		if (seat != null) {
			return seat;
		}

		Vec3d movement = Vec3d.ZERO;
		if (passenger.isPlayer()) {
			movement = playerMovement(passenger.asPlayer(), offset);
		}
		Vec3d targetXZ = VecUtil.rotatePitch(movement, -this.getRotationPitch());

		Vec3d rayStart = targetXZ.rotateYaw(-90).add(0, 1, 0);
		Vec3d rayDir = new Vec3d(0, -1, 0);

		Vec3d localTarget = targetXZ.rotateYaw(-90);

		double scale = this.gauge.scale();
		double bbOffset = 0.5 * scale;
		//Make able to pass 1m high slab
		IBoundingBox rayBox = IBoundingBox.from(
				localTarget.subtract(bbOffset, 2 * bbOffset, bbOffset),
				localTarget.add(bbOffset, 2 * bbOffset, bbOffset)
		);
		MeshNavigator navMesh = getDefinition().navigator;
		List<OBJFace> nearby = navMesh.getFloorFacesWithin(rayBox, scale);
		OptionalDouble targetY = nearby.stream()
									   .map(tri -> MathUtil.intersectRayTriangle(rayStart, rayDir, tri))
									   .filter(t -> t != null && t >= 0)
									   .mapToDouble(t -> rayStart.add(rayDir.scale(t)).y)
									   .max();

		if (targetY.isPresent()) {
			offset = VecUtil.rotatePitch(new Vec3d(targetXZ.x, targetY.getAsDouble(), targetXZ.z),
										 this.getRotationPitch());
		}
		return offset;
	}

	private Vec3d restrictPassengerPosition(Vec3d offset) {
		MeshNavigator navMesh = this.getDefinition().navigator;
		double scale = gauge.scale();
		offset = offset.scale(scale);

		Vec3d realOffset = offset.rotateYaw(-90);

		List<OBJFace> faces = navMesh.getAllFloorFaces(scale);
		//Check if we could move downward first
		Vec3d direction = new Vec3d(0, -1, 0);
		OptionalDouble shadowDown = faces.stream()
										 .map(face -> MathUtil.intersectRayTriangle(realOffset, direction, face))
										 .filter(t -> t != null && t >= 0)
										 .mapToDouble(t -> realOffset.add(direction.scale(t)).y)
										 .max();

		if (shadowDown.isPresent() && (offset.y - shadowDown.getAsDouble()) <= 1) {
			return new Vec3d(offset.x, shadowDown.getAsDouble(), offset.z);
		}

		Optional<Vec3d> closestPoint = faces.stream()
											.map(tri -> MathUtil.closestPointOnTriangle(realOffset, tri))
											.min(Comparator.comparingDouble(
													point -> realOffset.subtract(point).lengthSquared()));
		if (closestPoint.isPresent()) {
			offset = closestPoint.get().rotateYaw(90);
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
	private boolean isInternalDoorClosed(Vec3d start, Vec3d end) {
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
		if (movement.length() <= 0.1) {
			return offset;
		}

		movement = new Vec3d(movement.x, 0, movement.z).rotateYaw(this.getRotationYaw() - source.getRotationYawHead());
		Vec3d localOffset = offset.rotateYaw(-90).add(movement.rotateYaw(-90));

		double scale = this.gauge.scale();
		double bbOffset = 0.2 * scale;
		IBoundingBox rayBox = IBoundingBox.from(
				localOffset.subtract(bbOffset, bbOffset, bbOffset),
					localOffset.add(bbOffset, bbOffset, bbOffset)
		);
		MeshNavigator navMesh = getDefinition().navigator;
		List<OBJFace> nearbyCollision = navMesh.getCollisionFacesWithin(rayBox, scale);

		Vec3d rayStart = localOffset.add(0, 1, 0);
		Vec3d rayDir = movement.rotateYaw(-90).normalize();

		for (OBJFace tri : nearbyCollision) {
			Double t = MathUtil.intersectRayTriangle(rayStart, rayDir, tri);
			if (t != null && t >= 0) {
				return offset;
			}
		}

		if (isInternalDoorClosed(offset, movement)) {
			return offset;
		}

		offset = restrictPassengerPosition(offset.add(movement));

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

			boolean isAtFront = isAtCouplerWithFloor(offset, movement);
			boolean isAtBack =  isAtCouplerWithFloor(offset, movement);
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

	private boolean isAtCouplerWithFloor(Vec3d offset, Vec3d movement) {
		MeshNavigator navMesh = getDefinition().navigator;
		Vec3d finalOffset = offset.add(movement).rotateYaw(-90);
		return navMesh.atCarEnds(finalOffset);
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
