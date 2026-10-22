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
        private const val DATABASE_VERSION = 6

        private const val BCRYPT_COSTO = 10

        // Tabla Roles
        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "rol_id"
        private const val ROL_NOMBRE = "rol_nombre"

        // Tabla Usuarios
        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "usu_id"
        private const val USU_NOMBRE = "usu_nombre"
        private const val USU_CORREO = "usu_correo"
        private const val USU_CONTRASENA = "usu_contrasena"
        private const val USU_ROL = "usu_rol"

        // Query: Crear tabla Roles
        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                    "$ROL_ID INTEGER PRIMARY KEY, " +
                    "$ROL_NOMBRE TEXT NOT NULL)"

        // Query: Crear tabla Usuarios
        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                    "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USU_NOMBRE TEXT NOT NULL, " +
                    "$USU_CORREO TEXT NOT NULL UNIQUE, " +
                    "$USU_CONTRASENA TEXT NOT NULL, " +
                    "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        // Query: Precargar Roles
        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                    "(${Rol.ID_OBSERVADOR}, 'Observador de Pasillo'), " +
                    "(${Rol.ID_JEFE_TIENDA}, 'Jefe de Tienda'), " +
                    "(${Rol.ID_ADMINISTRADOR}, 'Administrador')"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_ROLES)

        // Hasheamos la contraseña inicial respetando mayúsculas y símbolos
        val hashInicial = BCrypt.withDefaults().hashToString(BCRYPT_COSTO, "Rincor.2026!".toCharArray())

        val sqlPrecargarUsuarios =
            "INSERT INTO $TABLA_USUARIOS ($USU_NOMBRE, $USU_CORREO, $USU_CONTRASENA, $USU_ROL) VALUES " +
                    "('Tamara Ramírez', 'observador@rinconsur.cl', '$hashInicial', ${Rol.ID_OBSERVADOR}), " +
                    "('Rodrigo Saavedra', 'supervisor@rinconsur.cl', '$hashInicial', ${Rol.ID_JEFE_TIENDA}), " +
                    "('Administrador IT', 'admin@rinconsur.cl', '$hashInicial', ${Rol.ID_ADMINISTRADOR})"

        db.execSQL(sqlPrecargarUsuarios)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
    }

    // ---------------------------------------------------------
    // METODOS DE CONSULTA Y ACTUALIZACION
    // ---------------------------------------------------------

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
        val correoLimpio = correo.trim().lowercase()

        val cursor = db.rawQuery(
            "SELECT $USU_CONTRASENA FROM $TABLA_USUARIOS WHERE $USU_CORREO = ?",
            arrayOf(correoLimpio)
        )

        var existe = false
        if (cursor.moveToFirst()) {
            val hashGuardado = cursor.getString(0)
            val verificacion = BCrypt.verifyer().verify(contrasenaPlana.toCharArray(), hashGuardado.toCharArray())
            existe = verificacion.verified
        }
        cursor.close()
        return existe
    }

    fun existeCorreo(correo: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLA_USUARIOS WHERE $USU_CORREO = ?",
            arrayOf(correo.trim().lowercase())
        )
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }

    fun buscarPorCorreo(correo: String): Usuario? {
        val db = this.readableDatabase
        val correoLimpio = correo.trim().lowercase()

        val cursor = db.rawQuery(
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE " +
                    "FROM $TABLA_USUARIOS u " +
                    "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID " +
                    "WHERE u.$USU_CORREO = ?",
            arrayOf(correoLimpio)
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
    fun buscarPorId(id: Long): Usuario? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT u.*, r.rol_nombre AS rol_nombre " +
                    "FROM usuarios u " +
                    "INNER JOIN roles r ON u.usu_rol = r.rol_id " +
                    "WHERE u.usu_id = ?",
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

    fun actualizarContrasena(correo: String, nuevaContrasenaPlana: String) {
        val db = this.writableDatabase
        val correoLimpio = correo.trim().lowercase()

        val hashNuevo = BCrypt.withDefaults().hashToString(BCRYPT_COSTO, nuevaContrasenaPlana.toCharArray())

        val valores = ContentValues().apply {
            put(USU_CONTRASENA, hashNuevo)
        }

        db.update(
            TABLA_USUARIOS,
            valores,
            "$USU_CORREO = ?",
            arrayOf(correoLimpio)
        )

    }
}