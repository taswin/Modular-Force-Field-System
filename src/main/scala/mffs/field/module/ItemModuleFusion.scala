package mffs.field.module

import mffs.GraphFrequency
import mffs.api.machine.Projector
import mffs.api.modules.Module.ProjectState
import mffs.base.ItemModule
import mffs.field.BlockProjector
import nova.core.block.Block
import nova.core.util.transform.Vector3i

/**
 * The fusion module attempts to fuse a force field with another.
 * This is done by checking all other force fields with THE SAME FREQUENCY,
 * and trying to fuse them together. 
 * @author Calclavia
 */
class ItemModuleFusion extends ItemModule {
	setCost(1f)
	setMaxCount(1)

	override def getID: String = "moduleFusion"

	override def onProject(projector: Projector, position: Vector3i): ProjectState = {
		if (GraphFrequency.instance
			.get(projector.getFrequency)
			.view
			.collect { case proj: BlockProjector => proj}
			.filter(_.world() == projector.asInstanceOf[Block].world())
			.filter(_.isActive)
			.filter(_.getShapeItem != null)
			.exists(_.isInField(position.toDouble))) {
			return ProjectState.skip
		}

		return ProjectState.pass
	}
}