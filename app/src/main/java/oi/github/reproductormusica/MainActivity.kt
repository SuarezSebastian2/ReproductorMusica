package oi.github.reproductormusica

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton

/**
 * Clase principal de la aplicación.
 */
class MainActivity : AppCompatActivity() {

    // Almacena la información de la canción actualmente seleccionada
    private lateinit var cancionActual: String

    // Índice de la canción actual en la lista de canciones
    private var cancionActualindex = 0
        set(value) {
            // Si el valor pasado es -1, selecciona la última canción de la lista, de lo contrario,
            // ajusta el índice de la canción actual para asegurarse de que siempre esté dentro de los límites
            // de la lista de canciones
            val v = if(value==-1){
                canciones.size-1
            }else{
                value % canciones.size
            }
            field = v
            cancionActual = canciones[v]
        }

    // Lista de todas las canciones disponibles
    private val canciones by lazy{
        // Obtiene una lista de todos los archivos del directorio de activos que contienen la extensión ".mp3"
        val nombreFicheros = assets.list("")?.toList() ?: listOf()
        nombreFicheros.filter { it.contains(".mp3") }
    }

    // Descriptor de archivo abierto para la canción actual
    private val fd by lazy {
        // Obtiene un objeto FileDescriptor para la canción actualmente seleccionada
        assets.openFd(cancionActual)
    }

    // Reproductor de medios para reproducir la canción actual
    private val mp by lazy {
        val m = MediaPlayer()
        m.setDataSource(
            // Configura el origen de datos del reproductor de medios como el archivo de la canción actual
            fd.fileDescriptor,
            fd.startOffset,
            fd.length
        )
        fd.close()
        m.prepare()
        m
    }

    // Lista de todos los controles de la interfaz de usuario
    private val controllers by lazy {
        // Obtiene una lista de todos los botones en la actividad
        listOf(R.id.play,R.id.stop,R.id.forward,R.id.previous).map {
            findViewById<MaterialButton>(it)
        }
    }

    // Constantes que representan la posición de cada botón en la lista de controles
    object ci{
        const val play = 0
        const val stop = 1
        const val forward = 2
        const val previous = 3
    }

    // TextView que muestra el nombre de la canción actualmente seleccionada
    private val nombrecancion by lazy {
        findViewById<TextView>(R.id.nomcancion)
    }

    /**
     * Función llamada cuando se crea la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configura el diseño de la actividad
        setContentView(R.layout.activity_main)

        // Configura los eventos de clic para cada botón de control
        controllers[ci.play].setOnClickListener(this::playClicked)
        controllers[ci.stop].setOnClickListener(this::stopClicked)
        controllers[ci.forward].setOnClickListener(this::nextclicked)
        controllers[ci.previous].setOnClickListener(this::prefclicked)

        /**
         * Actualiza el nombre de la canción actual en la vista correspondiente.
         */
            cancionActual = canciones!![cancionActualindex]
            nombrecancion.text = cancionActual
        }

        /**
         * Método que se llama cuando se hace clic en el botón "Play".
         * Inicia la reproducción de la canción actual si no está sonando o la pausa si ya lo está.
         * También cambia el icono del botón a "Pausa" o "Play" según corresponda.
         *
         * @param v La vista que generó el evento de clic (en este caso, el botón "Play").
         */
        private fun playClicked(v: View) {
            if (!mp.isPlaying) {
                mp.start()
                controllers[ci.play].setIconResource(R.drawable.ic_baseline_pause_48)
            } else {
                mp.pause()
                controllers[ci.play].setIconResource(R.drawable.tocar)
            }
        }

        /**
         * Método que se llama cuando se hace clic en el botón "Stop".
         * Detiene la reproducción de la canción actual, regresando al principio de la misma.
         * También cambia el icono del botón a "Play".
         *
         * @param v La vista que generó el evento de clic (en este caso, el botón "Stop").
         */
        private fun stopClicked(v: View) {
            if (mp.isPlaying) {
                mp.pause()
                controllers[ci.play].setIconResource(R.drawable.tocar)
            }
            mp.seekTo(0)
        }

        /**
         * Método que se llama cuando se hace clic en el botón "Siguiente".
         * Avanza a la siguiente canción en la lista de canciones y actualiza la vista correspondiente.
         *
         * @param v La vista que generó el evento de clic (en este caso, el botón "Siguiente").
         */
        private fun nextclicked(v: View) {
            cancionActualindex++
            refreshSong()
        }

        /**
         * Método que se llama cuando se hace clic en el botón "Anterior".
         * Retrocede a la canción anterior en la lista de canciones y actualiza la vista correspondiente.
         *
         * @param v La vista que generó el evento de clic (en este caso, el botón "Anterior").
         */
        private fun prefclicked(v: View) {
            cancionActualindex--
            refreshSong()
        }

        /**
         * Actualiza el reproductor con la información de la canción actual y comienza a reproducirla.
         * También actualiza el nombre de la canción en la vista correspondiente y cambia el icono del botón "Play" a "Pausa".
         */
        private fun refreshSong() {
            mp.reset()
            val fd = assets.openFd(cancionActual)
            mp.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            mp.prepare()
            playClicked(controllers[ci.play])
            nombrecancion.text = cancionActual
        }

}