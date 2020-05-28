package mx.edu.ittepic.ladm_u4_practica3_edgarramirez

import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    //Variables globales necesarias.
    var listaDatos = ArrayList<String>()
    var listaID = ArrayList<String>()
    var nombreBD = "celulares"
    var hilo: Hilo? = null

    val siPermisos = 700

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Solicitar los permisos, en caso de que no hayan sido otorgados.
        var permisoSendSMS =
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
        var permisoReadSMS =
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)
        var permisoReceiveSMS =
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS)

        if (permisoSendSMS != PackageManager.PERMISSION_GRANTED || permisoReadSMS != PackageManager.PERMISSION_GRANTED ||
                permisoReceiveSMS != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                            android.Manifest.permission.SEND_SMS, android.Manifest.permission.READ_SMS,
                            android.Manifest.permission.RECEIVE_SMS
                    ), siPermisos
            )
        }

        // Actualizar la lista de celulares
        cargarLista()

        // Guardar los celulares
        btnGuardar.setOnClickListener {
            // VALIDAR QUE LOS CAMPOS NO ESTÉN VACÍOS
            if (txtMarca.text.toString().isEmpty() ||
                    txtModelo.text.toString().isEmpty() ||
                    txtPrecio.text.toString().isEmpty()
            ) {
                mensaje("POR FAVOR LLENE TODOS LOS CAMPOS")
                return@setOnClickListener
            }

            guardarRegistro()
            // DESPUÉS DE GUARDAR, EL FOCUS QUEDA EN EL MODELO
            txtModelo.requestFocus()
        }

        // LIMPIAR LAS CAJAS DE TEXTO
        btnLimpiar.setOnClickListener {
            txtMarca.setText("")
            txtModelo.setText("")
            txtPrecio.setText("")
            txtMarca.requestFocus()
        }

        // MOSTRAR EL ÚLTIMO MENSAJE
        lblMensaje.setOnClickListener {
            ultimoMsj()
        }

        // EJECUTAR EL HILO
        hilo = Hilo(this)
        hilo!!.start()

    }

    // PERMISOS OTORGADOS
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == siPermisos) {
            ultimoMsj()
        }
    }

    // CARGAR LA LISTA.
    private fun cargarLista() {
        listaDatos.clear()
        listaID.clear()
        try {
            val cursor = BaseDatos(this, nombreBD, null, 1).readableDatabase
                    .rawQuery("SELECT * FROM CELULAR", null)
            var resultado = ""

            if (cursor.moveToFirst()) {
                do {
                    resultado = "\nMARCA: " + cursor.getString(1) + "\n" +
                            "MODELO: " + cursor.getString(2) + "\n" +
                            "PRECIO: " + cursor.getFloat(3)
                    cursor.getString(1)
                    listaDatos.add(resultado)
                    listaID.add(cursor.getString(0))
                } while (cursor.moveToNext())
            } else {
                // NO TIENE NADA EL CURSOR
                listaDatos.add("NO SE ENCONTRARON RESULTADOS")
            }
            // LLENAMOS LA LISTA DE REGISTROS.
            var adaptador =
                    ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaDatos)
            listaCelulares.adapter = adaptador

            listaCelulares.setOnItemClickListener { parent, view, position, id ->
                AlertDialog.Builder(this)
                        .setTitle("ATENCIÓN")
                        .setMessage("¿DESEA ELIMINAR EL CELULAR SELECCIONADO?\n" + listaDatos[position])
                        .setPositiveButton("ELIMINAR") { d, i ->
                            eliminarCelular(listaID[position])
                        }
                        .setNegativeButton("CANCELAR") { d, i -> }
                        .show()
            }
        } catch (err: SQLiteException) {
            Toast.makeText(this, err.message, Toast.LENGTH_LONG)
                    .show()
        }
    }

    // FUNCIÓN PARA ELIMINAR UN CELULAR
    private fun eliminarCelular(id: String) {
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var eliminar = baseDatos.writableDatabase
            var SQL = "DELETE FROM CELULAR WHERE ID = ?"
            var parametros = arrayOf(id)
            eliminar.execSQL(SQL, parametros)
            baseDatos.close()

            mensaje("SE ELIMINÓ")
        } catch (e: SQLiteException) {
            mensaje(e.message!!)
        }
        // ACTUALIZAR LA LISTA
        cargarLista()
    }

    // FUNCIÓN PARA GUARDAR LOS CELULARES
    private fun guardarRegistro() {
        var modelo = txtModelo.text.toString().toUpperCase()
        var marca = txtMarca.text.toString().toUpperCase()
        var precio = txtPrecio.text.toString().toDouble()

        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var insertar = baseDatos.writableDatabase
            var SQL = "INSERT INTO CELULAR VALUES(NULL,'${marca}','${modelo}','${precio}')"
            insertar.execSQL(SQL)
            baseDatos.close()
        } catch (e: SQLiteException) {
            mensaje(e.message!!)
        }
        // Actualizar lista
        cargarLista()
        mensaje("SE GUARDÓ EL REGISTRO")
    }

    // FUNCIÓN PARA CONSULTAR EL ÚLTIMO MENSAJE RECIBIDO.
    fun ultimoMsj() {
        try {
            val cursor = BaseDatos(this, nombreBD, null, 1).readableDatabase
                    .rawQuery("SELECT * FROM ENTRANTES", null)

            var ultimo = ""
            // SI EL CURSOR MUESTRA RESULTADOS, LLENAMOS LA VARIABLE PARA MOSTRAR EL ÚLTIMO MENSAJE.
            if (cursor.moveToFirst()) {
                var contestado = ""

                // IF PARA SABER SI EL MENSAJE YA FUE CONTESTADO.
                if (cursor.getString(2).equals("0")) {
                    contestado = "NO"
                } else {
                    contestado = "SÍ"
                }
                do {
                    ultimo = "ÚLTIMO MENSAJE RECIBIDO:\nCELULAR ORIGEN: " +
                            cursor.getString(0) + "\nMENSAJE SMS: " +
                            cursor.getString(1) + "\n¿FUE CONTESTADO?: " + contestado
                } while (cursor.moveToNext())
            } else {
                // SI LA VARIABLE CURSOR ESTÁ VACÍA, NO HAY DATOS.
                ultimo = "SIN MENSAJES. TABLA VACÍA."
            }
            lblMensaje.setText(ultimo)
        } catch (err: SQLiteException) {
            Toast.makeText(this, err.message, Toast.LENGTH_LONG).show()
        }
    }

    // FUNCIÓN PARA MOSTRAR MENSAJES
    private fun mensaje(m: String) {
        Toast.makeText(this, m, Toast.LENGTH_LONG)
                .show()
    }

    // ENVIAR EL MENSAJE
    fun enviarSMS() {
        var marca = ""
        var presupuesto = "0"
        var cadena ="ERROR"

        try {
            val cursor = BaseDatos(this, nombreBD,null,1).readableDatabase
                    .rawQuery("SELECT * FROM ENTRANTES",null)

            var ultimoNum = ""
            var ultimoMsj = ""
            var contestado = ""

            if(cursor.moveToFirst()) {
                do {
                    ultimoNum = cursor.getString(0)
                    ultimoMsj = cursor.getString(1)
                    contestado = cursor.getString(2)

                    if (validarSintaxisMsj(ultimoMsj) && ultimoMsj.split("-".toRegex(),3).size == 3) {
                        // SI ENTRA AQUÍ ES PORQUE LA SINTEXIS DEL MENSAJE SÍ ES CORRECTA.
                        var mensajeRecibido = ultimoMsj.split("-".toRegex(),3)
                        marca = mensajeRecibido[1]
                        presupuesto = mensajeRecibido[2]

                        // RECOPILAMOS LOS CELULARES DE ACUERDO AL PRESUPUESTO Y MARCA.
                        cadena = buscarCelular(marca, presupuesto)

                        if (contestado.equals("0")) {
                            // EN CASO DE QUE EL MENSAJE AÚN NO SE HA CONTESTADO
                            SmsManager.getDefault().sendTextMessage(ultimoNum, null, cadena, null, null)
                            actualizarContestado(ultimoNum)
                        }
                    } else{
                        if (contestado.equals("0")) {
                            // EL MENSAJE NO SE HA CONTESTADO, PERO LA SINTAXIS NO ES CORRECTA
                            cadena = "ERROR, LA SINTAXIS DEBERÍA DE SER: CELULAR-MOTOROLA-5000, por ejemplo."
                            SmsManager.getDefault().sendTextMessage(ultimoNum,null,cadena,null,null)
                            actualizarContestado(ultimoNum)
                        }
                    }
                }while(cursor.moveToNext())
            }
            cursor.close()
        }catch (error: SQLiteException){
            Toast.makeText(this,error.message,Toast.LENGTH_LONG).show()
        }
    }

    // VALIDAR EL MENSAJE, QUE TENGA LA SINTAXIS CORRECTA
    fun validarSintaxisMsj(m: String): Boolean {
        try {
            // DIVIDE EL MENSAJE EN PARTES, PARA COMPARAR LA PRIMER PALABRA
            var mensaje = m.split("-".toRegex(), 3)
            var celular = mensaje[0].toUpperCase()

            if (celular.equals("CELULAR") && mensaje.size == 3) {
                return true
            }
            return false
        } catch (e: IndexOutOfBoundsException) {
            return false
        }
        return false
    }

    // BUSCAR COINCIDENCIAS
    fun buscarCelular(marca: String, presupuesto: String): String {
        var resultado = ""
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM CELULAR WHERE MARCA = ? AND PRECIO <= ?"
            var parametros = arrayOf(marca.toUpperCase(), presupuesto)
            var cursor = select.rawQuery(SQL, parametros)

            if (cursor.moveToFirst()) {
                do {
                    // SI HAY RESULTADO.
                    resultado += "MODELO: " + cursor.getString(2) + " | PRECIO: " + cursor.getFloat(
                            3
                    ) +
                            " // "
                } while (cursor.moveToNext())
            } else {
                // NO ENCONTRÓ DATOS PARA ESE NÚMERO DE CONTROL Y UNIDAD
                resultado =
                        "NO HAY CELULARES DE ESA MARCA Y/O PRECIO. INTENTE CON OTRA MARCA, U OTRO PRESUPUESTO."
            }
            select.close()
            baseDatos.close()
        } catch (error: SQLiteException) {
        }

        return resultado
    }

    // ASIGNAR QUE EL MENSAJE YA FUE CONTESTADO.
    fun actualizarContestado (numero:String){
        // SI TIENE UN 0 AÚN NO SE HA CONTESTADO
        // SI TIENE UN 1 YA SE HA CONTESTADO
        try{
            var baseDatos = BaseDatos(this,nombreBD,null,1)
            var insertar = baseDatos.writableDatabase
            var SQL = "UPDATE ENTRANTES SET CONTESTADO ='1' WHERE NUMERO = ?"
            var parametros = arrayOf(numero)
            insertar.execSQL(SQL,parametros)
            insertar.close()
            baseDatos.close()
        }catch (error:SQLiteException){
            Toast.makeText(this,error.message,Toast.LENGTH_LONG).show()
        }
    }
}