import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.PulseEngineGame
import no.njoh.pulseengine.widgets.cli.CommandLine
import no.njoh.pulseengine.widgets.editor.SceneEditor
import no.njoh.pulseengine.widgets.profiler.Profiler

fun main() = PulseEngine.run(Main::class)

class Main : PulseEngineGame()
{
    override fun onCreate()
    {
        engine.widget.add(CommandLine(), Profiler(), SceneEditor())
        engine.config.fixedTickRate = 10
        engine.scene.addSystem(Server())
        engine.scene.start()
    }

    override fun onFixedUpdate() { }

    override fun onUpdate() { }

    override fun onRender() { }

    override fun onDestroy() { }
}