package mffs.field.mobilize

import nova.core.block.Block
import nova.core.game.Game
import nova.core.util.transform.Vector3i
import nova.core.world.World

import scala.collection.convert.wrapAll._

@deprecated
class BlockDropDelayedEvent(ticks: Int, block: Block, world: World, position: Vector3i) extends DelayedEvent(ticks) {
	protected override def onEvent {
		if (Game.instance.networkManager.isServer) {
			val checkBlock = world.getBlock(position)

			if (checkBlock.isPresent && checkBlock.get == block) {
				val drops = block.getDrops
				drops.foreach(drop => world.createEntity(position.toDouble + 0.5, drop))
				world.setBlock(position, Game.instance.blockManager.getAirBlock)
			}
		}
	}
}