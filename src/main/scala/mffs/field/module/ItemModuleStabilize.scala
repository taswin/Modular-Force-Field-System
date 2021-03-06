package mffs.field.module

import java.util

import mffs.api.Blacklist
import mffs.api.machine.Projector
import mffs.api.modules.Module.ProjectState
import mffs.base.{ItemModule, PacketBlock}
import mffs.content.Content
import mffs.field.BlockProjector
import mffs.field.shape.ItemShapeCustom
import nova.core.block.Block
import nova.core.game.Game
import nova.core.inventory.Inventory
import nova.core.inventory.components.SidedInventoryProvider
import nova.core.item.ItemBlock
import nova.core.util.Direction
import nova.core.util.transform.{Quaternion, Vector3i}
import nova.core.world.World

import scala.collection.convert.wrapAll._
class ItemModuleStabilize extends ItemModule {
	private var blockCount = 0

	setMaxCount(1)
	setCost(20)

	override def getID: String = "moduleStabilize"

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		blockCount = 0
		return false
	}

	override def onProject(projector: Projector, position: Vector3i): ProjectState = {
		val proj = projector.asInstanceOf[BlockProjector]
		val world = proj.world()

		/**
		 * Handle custom shape block placement
		 */
		val sampleBlock: Block = {
			if (projector.getShape.isInstanceOf[ItemShapeCustom] && !(projector.getModuleCount(Content.moduleCamouflage) > 0)) {
				val fieldBlockMap = projector.getShape.asInstanceOf[ItemShapeCustom].getStructure.getBlockStructure
				val fieldCenter = proj.position + projector.getTranslation
				val relativePosition = position - fieldCenter
				relativePosition.transform(Quaternion.fromEuler(-projector.getRotationYaw, -projector.getRotationPitch, 0))
				fieldBlockMap(relativePosition)
			} else {
				null
			}
		}

		//Find adjacent inventories and the first block to place down
		val inventories = getAdjacentInventories(world, proj.position)

		//Create a partial function condition based on sampleBlock
		val condition: PartialFunction[Block, Block] = {
			case block =>
				sampleBlock match {
					case sample if sample.sameType(block) && !Blacklist.stabilizationBlacklist.contains(sample) =>
						//TODO: Check moduleApproximation, placement conditions
						block
					//TODO: Check fruitless None matching
					case _ => block
				}
		}

		//Finds the most optimal block for placement
		val optimalInvItemPair = findOptimalBlock(inventories, condition)

		optimalInvItemPair match {
			case Some((inv: Inventory, item: ItemBlock)) =>
				try {
					val block = item.block
					//TODO: Call block stabilize event
					//Do the block placement
					inv.remove(item.withAmount(1))
					//copyStack.getItem.asInstanceOf[ItemBlock].placeBlockAt(copyStack, null, world, position.xi, position.yi, position.zi, 0, 0, 0, 0, metadata)
					world.setBlock(position, block)
					Game.instance.networkManager.sync(PacketBlock.effect2, proj)

					blockCount += 1

					if (blockCount >= projector.getModuleCount(Content.moduleSpeed) / 3) {
						return ProjectState.skip
					}
					else {
						return ProjectState.pass
					}
				}
				catch {
					case e: Exception => {
						Game.instance.logger.error("Stabilizer failed to place item '" + item + "'. The item or block may not have correctly implemented the placement methods.")
						e.printStackTrace()
					}
				}
			case _ =>
			//Cannot find any block to stabilize
		}

		return ProjectState.pass
	}

	def getAdjacentInventories(world: World, pos: Vector3i): Iterable[Inventory] =
		Direction.DIRECTIONS
			.view
			.map(dir => (dir, dir.toVector + pos))
			.map(kv => (kv._1, world.getBlock(kv._2)))
			.collect { case kv if kv._2.isPresent => (kv._1, kv._2.get) }
			.collect { case (dir: Direction, inven: SidedInventoryProvider) => inven.getInventory(dir.opposite) }
			.flatten

	def findOptimalBlock(inventories: Iterable[Inventory], condition: PartialFunction[Block, Block]): Option[(Inventory, ItemBlock)] = {
		inventories
			.collect {
			case inventory => (inventory,
				inventory.collectFirst {
					case item: ItemBlock => item
				})
		}
			.collectFirst { case (inv, Some(item)) => (inv, item) }
	}

	/*
	private def isApproximationEqual(block: Block, checkStack: Item): Boolean = {
		return block == Blocks.grass && (checkStack.getItem.asInstanceOf[ItemBlock]).field_150939_a == Blocks.dirt
	}*/

	override def getFortronCost(amplifier: Float): Float = super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier)
}