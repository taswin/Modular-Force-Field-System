package mffs.base

import java.util.Optional

import com.resonant.core.prefab.block.Rotatable
import com.resonant.wrapper.core.Placeholder
import mffs.api.machine.IActivatable
import mffs.content.Textures
import nova.core.block.Block
import nova.core.block.components.{DynamicRenderer, ItemRenderer, Stateful}
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.gui.KeyManager.Key
import nova.core.network.{Packet, PacketHandler}
import nova.core.render.texture.Texture
import nova.core.retention.{Storable, Stored}
import nova.core.util.Direction
import nova.core.util.transform.{Vector3d, Vector3i}

/**
 * A base block class for all MFFS blocks to inherit.
 * @author Calclavia
 */
abstract class BlockMachine extends Block with PacketHandler with IActivatable with Stateful with Storable with DynamicRenderer with ItemRenderer {
	/**
	 * Used for client side animations.
	 */
	var animation = 0d

	/**
	 * Is this machine switched on internally via GUI?
	 */
	@Stored
	var isRedstoneActive = false

	/**
	 * Is the machine active and working?
	 */
	@Stored
	private var active = false

	//	stepSound = Block.soundTypeMetal

	override def getHardness: Double = Double.PositiveInfinity

	override def getResistance: Double = 100

	override def getTexture(side: Direction): Optional[Texture] = Optional.of(Textures.machine)

	override def isOpaqueCube: Boolean = false

	//	override def getExplosionResistance(entity: Entity): Float = 100

	override def read(packet: Packet) {
		super.read(packet)

		if (packet.getID == PacketBlock.description) {
			val prevActive = active
			active = packet.readBoolean()
			isRedstoneActive = packet.readBoolean()

			if (prevActive != this.active) {
				world.markStaticRender(position())
			}
		}
		else if (packet.getID == PacketBlock.toggleActivation) {
			isRedstoneActive = !isRedstoneActive

			if (isRedstoneActive) {
				setActive(true)
			}
			else {
				setActive(false)
			}
		}
	}

	def setActive(flag: Boolean) {
		active = flag
		world().markStaticRender(position())
	}

	//TODO: Implement redstone support

	override def write(packet: Packet) {
		super.write(packet)

		if (packet.getID == PacketBlock.description) {
			packet <<< active
			packet <<< isRedstoneActive
		}
	}

	override def onNeighborChange(neighborPosition: Vector3i) = {
		if (Game.instance.networkManager.isServer) {
			if (isPoweredByRedstone) {
				powerOn()
			}
			else {
				powerOff()
			}
		}
	}

	def powerOn() {
		this.setActive(true)
	}

	def powerOff() {
		if (!this.isRedstoneActive && Game.instance.networkManager.isServer) {
			this.setActive(false)
		}
	}

	def isPoweredByRedstone: Boolean = false

	def isActive: Boolean = active

	override def onRightClick(entity: Entity, side: Int, hit: Vector3d): Boolean = {
		if (Placeholder.isHoldingConfigurator(entity)) {
			if (Game.instance.keyManager.isKeyDown(Key.KEY_LSHIFT)) {
				if (Game.instance.networkManager.isServer) {
					//TODO: Fix this
					// InventoryUtility.dropBlockAsItem(world, position)
					world.setBlock(position, null)
					return true
				}
				return false
			}
		}

		if (this.isInstanceOf[Rotatable]) {
			return this.asInstanceOf[Rotatable].rotate(side, hit)
		}

		return false
	}

	/**
	 * ComputerCraft

  def getType: String =
  {
    return this.getInvName
  }

  def getMethodNames: Array[String] =
  {
    return Array[String]("isActivate", "setActivate")
  }

  def callMethod(computer: Nothing, context: Nothing, method: Int, arguments: Array[AnyRef]): Array[AnyRef] =
  {
    method match
    {
      case 0 =>
      {
        return Array[AnyRef](this.isActive)
      }
      case 1 =>
      {
        this.setActive(arguments(0).asInstanceOf[Boolean])
        return null
      }
    }
    throw new Exception("Invalid method.")
  }

  def attach(computer: Nothing)
  {
  }

  def detach(computer: Nothing)
  {
  }*/
}
