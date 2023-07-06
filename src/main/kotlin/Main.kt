import no.njoh.pulseengine.core.PulseEngine
import no.njoh.pulseengine.core.PulseEngineGame
import no.njoh.pulseengine.core.graphics.api.Multisampling
import no.njoh.pulseengine.core.scene.SceneState
import no.njoh.pulseengine.widgets.cli.CommandLine
import no.njoh.pulseengine.widgets.editor.SceneEditor
import no.njoh.pulseengine.widgets.profiler.Profiler

fun main() = PulseEngine.run(Main::class)

class Main : PulseEngineGame()
{
    override fun onCreate()
    {
        engine.widget.add(CommandLine(), Profiler(), SceneEditor())
        engine.gfx.mainSurface.setMultisampling(Multisampling.MSAA16)
        engine.asset.loadFont("fonts/badabb.ttf", "font-bold")
        engine.data.saveDirectory = "E:\\Users\\Niklas\\Documents\\Projects\\aigame\\src\\main\\resources"
        engine.scene.loadAndSetActive("scenes/playground.scn")
        engine.scene.start()
    }

    override fun onFixedUpdate() { }

    override fun onUpdate() { }

    override fun onRender() { }

    override fun onDestroy()
    {
        if (engine.scene.state == SceneState.STOPPED)
            engine.scene.save()
    }
}