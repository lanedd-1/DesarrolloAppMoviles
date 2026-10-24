package com.example.rinconsur.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario

class RinconDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "rinconsur.db"
        private const val DATABASE_VERSION = 10

        private const val BCRYPT_COSTO = 10

        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "rol_id"
        private const val ROL_NOMBRE = "rol_nombre"

        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "usu_id"
        private const val USU_NOMBRE = "usu_nombre"
        private const val USU_CORREO = "usu_correo"
        private const val USU_CONTRASENA = "usu_contrasena"
        private const val USU_ROL = "usu_rol"

        private const val TABLA_SESIONES = "sesiones"
        private const val SES_ID = "ses_id"
        private const val SES_CODIGO = "ses_codigo"
        private const val SES_ACCESO = "ses_acceso"
        private const val SES_FECHA = "ses_fecha"
        private const val SES_ESTADO = "ses_estado"
        private const val SES_USU_ID = "ses_usu_id"

        private const val TABLA_REGISTROS = "registros_conteo"
        private const val REG_ID = "reg_id"
        private const val REG_SESION_ID = "reg_sesion_id"
        private const val REG_CLASIFICACION = "reg_clasificacion"
        private const val REG_RANGO_ETARIO = "reg_rango_etario"
        private const val REG_COMPANIA = "reg_compania"
        private const val REG_BOLSAS = "reg_bolsas"
        private const val REG_DESTINO = "reg_destino"
        private const val REG_HORA = "reg_hora"

        private const val TABLA_ACCESOS_CONFIG = "accesos_config"
        private const val ACC_NOMBRE = "acceso_nombre"
        private const val ACC_DISPONIBLE = "disponible"

        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES ($ROL_ID INTEGER PRIMARY KEY, $ROL_NOMBRE TEXT NOT NULL)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                    "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USU_NOMBRE TEXT NOT NULL, " +
                    "$USU_CORREO TEXT NOT NULL UNIQUE, " +
                    "$USU_CONTRASENA TEXT NOT NULL, " +
                    "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        private const val SQL_CREAR_SESIONES =
            "CREATE TABLE $TABLA_SESIONES (" +
                    "$SES_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$SES_CODIGO TEXT NOT NULL, " +
                    "$SES_ACCESO TEXT NOT NULL, " +
                    "$SES_FECHA TEXT NOT NULL, " +
                    "$SES_ESTADO TEXT NOT NULL, " +
                    "$SES_USU_ID INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($USU_ID))"

        private const val SQL_CREAR_REGISTROS =
            "CREATE TABLE $TABLA_REGISTROS (" +
                    "$REG_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$REG_SESION_ID INTEGER NOT NULL REFERENCES $TABLA_SESIONES($SES_ID), " +
                    "$REG_CLASIFICACION TEXT NOT NULL, " +
                    "$REG_RANGO_ETARIO TEXT NOT NULL, " +
                    "$REG_COMPANIA TEXT NOT NULL, " +
                    "$REG_BOLSAS INTEGER NOT NULL, " +
                    "$REG_DESTINO TEXT, " +
                    "$REG_HORA TEXT NOT NULL)"

        private const val SQL_CREAR_ACCESOS_CONFIG =
            "CREATE TABLE $TABLA_ACCESOS_CONFIG (" +
                    "$ACC_NOMBRE TEXT PRIMARY KEY, " +
                    "$ACC_DISPONIBLE INTEGER NOT NULL DEFAULT 1)"

        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                    "(${Rol.ID_OBSERVADOR}, 'Observador de Pasillo'), " +
                    "(${Rol.ID_JEFE_TIENDA}, 'Jefe de Tienda'), " +
                    "(${Rol.ID_ADMINISTRADOR}, 'Administrador')"

        private const val SQL_PRECARGAR_ACCESOS =
            "INSERT INTO $TABLA_ACCESOS_CONFIG ($ACC_NOMBRE, $ACC_DISPONIBLE) VALUES " +
                    "('Acceso A1', 1), " +
                    "('Acceso A2', 1), " +
                    "('Acceso A3', 1), " +
                    "('Acceso A4', 1)"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_CREAR_SESIONES)
        db.execSQL(SQL_CREAR_REGISTROS)
        db.execSQL(SQL_CREAR_ACCESOS_CONFIG)

        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_ACCESOS)

        val hashInicial = BCrypt.withDefaults().hashToString(BCRYPT_COSTO, "Rincor.2026!".toCharArray())

        val sqlPrecargarUsuarios =
            "INSERT INTO $TABLA_USUARIOS ($USU_NOMBRE, $USU_CORREO, $USU_CONTRASENA, $USU_ROL) VALUES " +
                    "('Tamara Ramírez', 'observador@rinconsur.cl', '$hashInicial', ${Rol.ID_OBSERVADOR}), " +
                    "('Rodrigo Saavedra', 'supervisor@rinconsur.cl', '$hashInicial', ${Rol.ID_JEFE_TIENDA}), " +
                    "('Administrador IT', 'admin@rinconsur.cl', '$hashInicial', ${Rol.ID_ADMINISTRADOR})"
        db.execSQL(sqlPrecargarUsuarios)

        val fechaHoy = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date())

        val sqlPrecargarSesiones = """
            INSERT INTO $TABLA_SESIONES ($SES_CODIGO, $SES_ACCESO, $SES_FECHA, $SES_ESTADO, $SES_USU_ID) VALUES 
            ('SES-A1-859', 'Acceso A1', '$fechaHoy', 'FINALIZADA', 1),
            ('SES-A2-442', 'Acceso A2', '$fechaHoy', 'FINALIZADA', 1),
            ('SES-A3-001', 'Acceso A3', '$fechaHoy', 'EN_CURSO', 1)
        """.trimIndent()
        db.execSQL(sqlPrecargarSesiones)

        val sqlPrecargarRegistros = """
            INSERT INTO $TABLA_REGISTROS ($REG_SESION_ID, $REG_CLASIFICACION, $REG_RANGO_ETARIO, $REG_COMPANIA, $REG_BOLSAS, $REG_DESTINO, $REG_HORA) VALUES 
            (1, 'VISITA', 'RANGO_26_40', 'SOLO', 0, NULL, '10:05:00'),
            (1, 'TRANSITO', 'RANGO_18_25', 'PAREJA', 1, 'A3', '10:12:00'),
            (1, 'VISITA', 'RANGO_41_60', 'CON_NINOS', 1, NULL, '10:15:00'),
            (1, 'VISITA', 'MENOR_18', 'GRUPO', 0, NULL, '10:20:00'),
            (1, 'TRANSITO', 'MAYOR_60', 'SOLO', 0, 'A2', '10:25:00'),
            (2, 'VISITA', 'RANGO_26_40', 'PAREJA', 1, NULL, '11:00:00'),
            (2, 'VISITA', 'RANGO_18_25', 'SOLO', 0, NULL, '11:10:00'),
            (2, 'TRANSITO', 'RANGO_41_60', 'SOLO', 0, 'A1', '11:15:00'),
            (2, 'VISITA', 'MAYOR_60', 'CON_NINOS', 0, NULL, '11:20:00'),
            (3, 'VISITA', 'RANGO_26_40', 'GRUPO', 1, NULL, '12:05:00'),
            (3, 'VISITA', 'RANGO_18_25', 'PAREJA', 1, NULL, '12:12:00'),
            (3, 'TRANSITO', 'MENOR_18', 'SOLO', 0, 'A1', '12:15:00')
        """.trimIndent()
        db.execSQL(sqlPrecargarRegistros)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ACCESOS_CONFIG")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_REGISTROS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_SESIONES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
    }

    fun crearUsuario(nombre: String, correo: String, contrasenaPlana: String, rolId: Long): Long {
        val db = this.writableDatabase
        val hash = BCrypt.withDefaults().hashToString(BCRYPT_COSTO, contrasenaPlana.toCharArray())
        val valores = ContentValues().apply {
            put(USU_NOMBRE, nombre.trim())
            put(USU_CORREO, correo.trim().lowercase())
            put(USU_CONTRASENA, hash)
            put(USU_ROL, rolId)
        }
        return db.insert(TABLA_USUARIOS, null, valores)
    }

    fun validarLogin(correo: String, contrasenaPlana: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $USU_CONTRASENA FROM $TABLA_USUARIOS WHERE $USU_CORREO = ?", arrayOf(correo.trim().lowercase()))
        var existe = false
        if (cursor.moveToFirst()) {
            val hashGuardado = cursor.getString(0)
            existe = BCrypt.verifyer().verify(contrasenaPlana.toCharArray(), hashGuardado.toCharArray()).verified
        }
        cursor.close()
        return existe
    }

    fun existeCorreo(correo: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLA_USUARIOS WHERE $USU_CORREO = ?", arrayOf(correo.trim().lowercase()))
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }

    fun buscarPorCorreo(correo: String): Usuario? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE FROM $TABLA_USUARIOS u INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID WHERE u.$USU_CORREO = ?",
            arrayOf(correo.trim().lowercase())
        )
        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
                correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
                contrasena = cursor.getString(cursor.getColumnIndexOrThrow(USU_CONTRASENA)),
                rolId = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
                rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE))
            )
        }
        cursor.close()
        return usuario
    }

    fun actualizarContrasena(correo: String, nuevaContrasenaPlana: String) {
        val db = this.writableDatabase
        val hashNuevo = BCrypt.withDefaults().hashToString(BCRYPT_COSTO, nuevaContrasenaPlana.toCharArray())
        val valores = ContentValues().apply { put(USU_CONTRASENA, hashNuevo) }
        db.update(TABLA_USUARIOS, valores, "$USU_CORREO = ?", arrayOf(correo.trim().lowercase()))
    }

    fun buscarPorId(id: Long): Usuario? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT u.*, r.rol_nombre AS rol_nombre FROM usuarios u INNER JOIN roles r ON u.usu_rol = r.rol_id WHERE u.usu_id = ?",
            arrayOf(id.toString())
        )
        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("usu_id")),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("usu_nombre")),
                correo = cursor.getString(cursor.getColumnIndexOrThrow("usu_correo")),
                contrasena = cursor.getString(cursor.getColumnIndexOrThrow("usu_contrasena")),
                rolId = cursor.getLong(cursor.getColumnIndexOrThrow("usu_rol")),
                rolNombre = cursor.getString(cursor.getColumnIndexOrThrow("rol_nombre"))
            )
        }
        cursor.close()
        return usuario
    }

    fun crearSesion(codigo: String, acceso: String, fecha: String, usuarioId: Long): Long {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(SES_CODIGO, codigo)
            put(SES_ACCESO, acceso)
            put(SES_FECHA, fecha)
            put(SES_ESTADO, "EN_CURSO")
            put(SES_USU_ID, usuarioId)
        }
        return db.insert(TABLA_SESIONES, null, valores)
    }

    fun guardarRegistroConteo(
        sesionId: Long, clasificacion: String, rangoEtario: String, compania: String, bolsas: Boolean, destino: String?, hora: String
    ): Long {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(REG_SESION_ID, sesionId)
            put(REG_CLASIFICACION, clasificacion)
            put(REG_RANGO_ETARIO, rangoEtario)
            put(REG_COMPANIA, compania)
            put(REG_BOLSAS, if (bolsas) 1 else 0)
            put(REG_DESTINO, destino)
            put(REG_HORA, hora)
        }
        return db.insert(TABLA_REGISTROS, null, valores)
    }

    fun finalizarSesion(idSesion: Long): Boolean {
        val db = this.writableDatabase
        val valores = ContentValues().apply { put(SES_ESTADO, "FINALIZADA") }
        val filasAfectadas = db.update(TABLA_SESIONES, valores, "$SES_ID = ?", arrayOf(idSesion.toString()))
        db.close()
        return filasAfectadas > 0
    }

    fun eliminarUltimoRegistro(sesionId: Long): Boolean {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT MAX(reg_id) FROM $TABLA_REGISTROS WHERE $REG_SESION_ID = ?", arrayOf(sesionId.toString()))
        var ultimoId: Long = -1
        if (cursor.moveToFirst()) {
            ultimoId = cursor.getLong(0)
        }
        cursor.close()

        if (ultimoId != -1L && ultimoId != 0L) {
            val filasAfectadas = db.delete(TABLA_REGISTROS, "$REG_ID = ?", arrayOf(ultimoId.toString()))
            return filasAfectadas > 0
        }
        return false
    }

    fun obtenerMuestrasRegistradasUsuario(usuarioId: Long): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(r.reg_id) FROM $TABLA_REGISTROS r INNER JOIN $TABLA_SESIONES s ON r.reg_sesion_id = s.ses_id WHERE s.ses_usu_id = ?"
        val cursor = db.rawQuery(query, arrayOf(usuarioId.toString()))
        var total = 0
        if (cursor.moveToFirst()) total = cursor.getInt(0)
        cursor.close()
        return total
    }

    fun obtenerEstadisticasSesiones(usuarioId: Long): Pair<Int, Int> {
        val db = this.readableDatabase
        var total = 0
        val cursorTotal = db.rawQuery("SELECT COUNT(ses_id) FROM $TABLA_SESIONES WHERE ses_usu_id = ?", arrayOf(usuarioId.toString()))
        if (cursorTotal.moveToFirst()) total = cursorTotal.getInt(0)
        cursorTotal.close()

        var completadas = 0
        val cursorFin = db.rawQuery("SELECT COUNT(ses_id) FROM $TABLA_SESIONES WHERE ses_usu_id = ? AND ses_estado = 'FINALIZADA'", arrayOf(usuarioId.toString()))
        if (cursorFin.moveToFirst()) completadas = cursorFin.getInt(0)
        cursorFin.close()

        return Pair(completadas, total)
    }

    fun obtenerSesionesPorUsuario(usuarioId: Long): List<SesionItem> {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ses_id, ses_codigo, ses_acceso, ses_fecha, ses_estado FROM $TABLA_SESIONES WHERE ses_usu_id = ? ORDER BY ses_id DESC",
            arrayOf(usuarioId.toString())
        )
        val listaItems = mutableListOf<SesionItem>()
        if (cursor.moveToFirst()) {
            do {
                listaItems.add(
                    SesionItem(
                        id = cursor.getLong(0),
                        codigo = cursor.getString(1),
                        acceso = cursor.getString(2),
                        fecha = cursor.getString(3),
                        estado = cursor.getString(4)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return listaItems
    }

    fun obtenerAccesosNoDisponibles(): List<String> {
        val db = this.readableDatabase
        val noDisponibles = mutableSetOf<String>()
        val cursorConfig = db.rawQuery("SELECT $ACC_NOMBRE FROM $TABLA_ACCESOS_CONFIG WHERE $ACC_DISPONIBLE = 0", null)
        if (cursorConfig.moveToFirst()) {
            do {
                noDisponibles.add(cursorConfig.getString(0))
            } while (cursorConfig.moveToNext())
        }
        cursorConfig.close()

        val cursorSesiones = db.rawQuery("SELECT $SES_ACCESO FROM $TABLA_SESIONES WHERE $SES_ESTADO != 'FINALIZADA'", null)
        if (cursorSesiones.moveToFirst()) {
            do {
                noDisponibles.add(cursorSesiones.getString(0))
            } while (cursorSesiones.moveToNext())
        }
        cursorSesiones.close()
        return noDisponibles.toList()
    }

    fun obtenerAccesosCerrados(): List<String> {
        val db = this.readableDatabase
        val cerrados = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT $ACC_NOMBRE FROM $TABLA_ACCESOS_CONFIG WHERE $ACC_DISPONIBLE = 0", null)
        if (cursor.moveToFirst()) {
            do {
                cerrados.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return cerrados
    }

    fun obtenerAccesosEnUso(): List<String> {
        val db = this.readableDatabase
        val enUso = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT $SES_ACCESO FROM $TABLA_SESIONES WHERE $SES_ESTADO != 'FINALIZADA'", null)
        if (cursor.moveToFirst()) {
            do {
                enUso.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return enUso
    }

    fun obtenerEstadoTodosAccesos(): Map<String, Boolean> {
        val db = this.readableDatabase
        val mapa = LinkedHashMap<String, Boolean>()
        val cursor = db.rawQuery("SELECT $ACC_NOMBRE, $ACC_DISPONIBLE FROM $TABLA_ACCESOS_CONFIG ORDER BY $ACC_NOMBRE ASC", null)
        if (cursor.moveToFirst()) {
            do {
                mapa[cursor.getString(0)] = cursor.getInt(1) == 1
            } while (cursor.moveToNext())
        }
        cursor.close()
        return mapa
    }

    fun cambiarDisponibilidadAcceso(accesoNombre: String, disponible: Boolean): Boolean {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(ACC_DISPONIBLE, if (disponible) 1 else 0)
        }
        val filas = db.update(TABLA_ACCESOS_CONFIG, valores, "$ACC_NOMBRE = ?", arrayOf(accesoNombre))
        return filas > 0
    }

    fun obtenerTotalMuestrasPorFecha(fecha: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(r.reg_id) FROM $TABLA_REGISTROS r INNER JOIN $TABLA_SESIONES s ON r.reg_sesion_id = s.ses_id WHERE s.ses_fecha = ?",
            arrayOf(fecha)
        )
        var total = 0
        if (cursor.moveToFirst()) total = cursor.getInt(0)
        cursor.close()
        return total
    }

    fun obtenerSesionesActivasPorFecha(fecha: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(ses_id) FROM $TABLA_SESIONES WHERE ses_fecha = ? AND ses_estado = 'EN_CURSO'",
            arrayOf(fecha)
        )
        var total = 0
        if (cursor.moveToFirst()) total = cursor.getInt(0)
        cursor.close()
        return total
    }

    fun obtenerFlujoPorAcceso(fecha: String): List<FlujoAcceso> {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT s.ses_acceso, COUNT(r.reg_id) AS cantidad " +
                    "FROM $TABLA_SESIONES s " +
                    "LEFT JOIN $TABLA_REGISTROS r ON s.ses_id = r.reg_sesion_id " +
                    "WHERE s.ses_fecha = ? " +
                    "GROUP BY s.ses_acceso " +
                    "ORDER BY cantidad DESC",
            arrayOf(fecha)
        )
        val lista = mutableListOf<FlujoAcceso>()
        if (cursor.moveToFirst()) {
            do {
                val acceso = cursor.getString(0)
                val cantidad = cursor.getInt(1)
                val estado = if (cantidad >= 20) "Alto Tráfico" else "Normal"
                lista.add(FlujoAcceso(acceso, cantidad, estado))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    // ¡AQUÍ ESTÁ LA CORRECCIÓN DE LA FUNCIÓN QUE CAUSÓ EL CRASH!
    fun obtenerFechasDisponibles(): List<String> {
        val db = this.readableDatabase
        val fechas = mutableListOf<String>()
        // Cambiamos DISTINCT por GROUP BY para que sea 100% compatible con SQLite al usar ORDER BY
        val cursor = db.rawQuery(
            "SELECT ses_fecha FROM $TABLA_SESIONES GROUP BY ses_fecha ORDER BY MAX(ses_id) DESC",
            null
        )
        if (cursor.moveToFirst()) {
            do {
                fechas.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return fechas
    }
    fun obtenerRegistrosPorAccesoYFecha(acceso: String, fecha: String): List<RegistroDetalleItem> {
        val db = this.readableDatabase
        val lista = mutableListOf<RegistroDetalleItem>()

        val query = """
            SELECT r.reg_id, r.reg_hora, r.reg_clasificacion, r.reg_rango_etario, r.reg_compania, r.reg_bolsas, r.reg_destino
            FROM $TABLA_REGISTROS r
            INNER JOIN $TABLA_SESIONES s ON r.reg_sesion_id = s.ses_id
            WHERE s.ses_acceso = ? AND s.ses_fecha = ?
            ORDER BY r.reg_id DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(acceso, fecha))
        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    RegistroDetalleItem(
                        id = cursor.getLong(0),
                        hora = cursor.getString(1),
                        clasificacion = cursor.getString(2),
                        rangoEtario = cursor.getString(3),
                        compania = cursor.getString(4),
                        bolsas = cursor.getInt(5) == 1,
                        destino = cursor.getString(6)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }
    fun eliminarRegistroPorId(idRegistro: Long): Boolean {
        val db = this.writableDatabase
        val filas = db.delete(TABLA_REGISTROS, "$REG_ID = ?", arrayOf(idRegistro.toString()))
        return filas > 0
    }

    fun actualizarRegistroEspecifico(
        idRegistro: Long,
        clasificacion: String,
        rangoEtario: String,
        compania: String,
        bolsas: Boolean,
        destino: String?
    ): Boolean {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(REG_CLASIFICACION, clasificacion)
            put(REG_RANGO_ETARIO, rangoEtario)
            put(REG_COMPANIA, compania)
            put(REG_BOLSAS, if (bolsas) 1 else 0)
            put(REG_DESTINO, destino)
        }
        val filas = db.update(TABLA_REGISTROS, valores, "$REG_ID = ?", arrayOf(idRegistro.toString()))
        return filas > 0
    }

}
data class RegistroDetalleItem(
    val id: Long,
    val hora: String,
    val clasificacion: String,
    val rangoEtario: String,
    val compania: String,
    val bolsas: Boolean,
    val destino: String?
)

data class SesionItem(
    val id: Long,
    val codigo: String,
    val acceso: String,
    val fecha: String,
    val estado: String
)

data class FlujoAcceso(
    val acceso: String,
    val cantidad: Int,
    val estado: String
)