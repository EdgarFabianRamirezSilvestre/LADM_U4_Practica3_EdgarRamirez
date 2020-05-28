package mx.edu.ittepic.ladm_u4_practica3_edgarramirez

// CLASE DE HILO PARA QUE SE ENVÍE EL MENSAJE
class Hilo (p:MainActivity) : Thread() {
    private var iniciar = false
    private var puntero = p

    override fun run() {
        super.run()
        iniciar = true
        while (iniciar) {
            sleep(1000)
            puntero.runOnUiThread {
                puntero.enviarSMS()
            }
        }
    }
}