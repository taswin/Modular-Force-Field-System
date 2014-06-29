package mffs

import java.io.File

import resonant.api.mffs.Blacklist
import cpw.mods.fml.common.Loader
import net.minecraftforge.common.config.{Configuration, Property}
import resonant.lib.config.Config
import resonant.lib.utility.LanguageUtility

/**
 * MFFS Configuration Settings
 *
 * @author Calclavia
 */
object Settings
{
  final val configuration: Configuration = new Configuration(new File(Loader.instance.getConfigDir, Reference.NAME + ".cfg"))
  final val maxFrequencyDigits: Int = 6

  @Config
  var maxForceFieldsPerTick: Int = 1000
  @Config
  var maxForceFieldScale: Int = 200
  @Config
  var fortronProductionMultiplier: Double = 1
  @Config(comment = "Should the interdiction matrix interact with creative players?.")
  var interdictionInteractCreative: Boolean = true
  @Config(comment = "Set this to false to turn off the MFFS Chunkloading capabilities.")
  var loadFieldChunks: Boolean = true
  @Config(comment = "Allow the operator(s) to override security measures created by MFFS?")
  var allowOpOverride: Boolean = true
  @Config(comment = "Cache allows temporary data saving to decrease calculations required.")
  var useCache: Boolean = true
  @Config(comment = "Turning this to false will make MFFS run without electricity or energy systems required. Great for vanilla!")
  var enableElectricity: Boolean = true
  @Config(comment = "Turning this to false will enable better client side packet and updates but in the cost of more packets sent.")
  var conservePackets: Boolean = true
  @Config(comment = "Turning this to false will reduce rendering and client side packet graphical packets.")
  var highGraphics: Boolean = true
  @Config(comment = "The energy required to perform a kill for the interdiction matrix.")
  var interdictionMatrixMurderEnergy: Int = 0
  @Config(comment = "The maximum range for the interdiction matrix.")
  var interdictionMatrixMaxRange: Int = Integer.MAX_VALUE
  @Config
  var enableForceManipulator: Boolean = true
  @Config
  var allowForceManipulatorTeleport: Boolean = true
  @Config
  var allowFortronTeleport: Boolean = true

  def load
  {
    configuration.load()

    val forceManipulatorBlacklist: Property = configuration.get(Configuration.CATEGORY_GENERAL, "Force Manipulator Blacklist", "")
    forceManipulatorBlacklist.comment = "Put a list of block IDs to be not-moved by the force manipulator. Separate by commas, no space."
    val blackListManipulate: String = forceManipulatorBlacklist.getString()
    Blacklist.forceManipulationBlacklist.addAll(LanguageUtility.decodeIDSplitByComma(blackListManipulate))
    val blacklist1: Property = configuration.get(Configuration.CATEGORY_GENERAL, "Stabilization Blacklist", "")
    val blackListStabilize: String = blacklist1.getString
    Blacklist.stabilizationBlacklist.addAll(LanguageUtility.decodeIDSplitByComma(blackListStabilize))
    val blacklist2: Property = configuration.get(Configuration.CATEGORY_GENERAL, "Disintegration Blacklist", "")
    val blackListDisintegrate: String = blacklist1.getString
    Blacklist.disintegrationBlacklist.addAll(LanguageUtility.decodeIDSplitByComma(blackListDisintegrate))

    configuration.save()
  }

}