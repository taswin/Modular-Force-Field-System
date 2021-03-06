package mffs

import java.util.{List => JList}

import com.resonant.core.graph.internal.Graph
import mffs.api.Frequency
import mffs.api.fortron.IServerThread

import scala.collection.convert.wrapAll._

object GraphFrequency {
	var client = new GraphFrequency
	var server = new GraphFrequency

	def instance: GraphFrequency = {
		val thr: Thread = Thread.currentThread
		//TODO: Check if this is correct
		if ((thr.getName == "Server thread") || thr.isInstanceOf[IServerThread]) {
			return server
		}
		return client
	}
}

class GraphFrequency extends Graph[Frequency] {

	private var nodes = Set.empty[Frequency]
	private var frequencyMap = Map.empty[Int, Set[Frequency]].withDefaultValue(Set.empty)

	override def add(node: Frequency) {
		nodes += node
		build()
	}

	override def build() {
		frequencyMap = Map.empty

		getNodes.map(n => (n.getFrequency, n)).foreach(kv =>
			frequencyMap += (kv._1 -> (frequencyMap(kv._1) + kv._2))
		)
	}

	override def getNodes: JList[Frequency] = nodes.toList

	override def remove(node: Frequency) {
		nodes -= node
		build()
	}

	def get(frequency: Int) = frequencyMap(frequency)
}