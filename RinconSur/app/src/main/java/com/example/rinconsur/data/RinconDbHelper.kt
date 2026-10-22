package com.example.rinconsur // Ajusta si lo tienes dentro de una carpeta como .database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario

class RinconDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "rinconsur.db"
        private const val DATABASE_VERSION = 2

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

        // Query: Precargar Usuarios
        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS ($USU_NOMBRE, $USU_CORREO, $USU_CONTRASENA, $USU_ROL) VALUES " +
                    "('Tamara Ramírez', 'observador@rinconsur.cl', '123456', ${Rol.ID_OBSERVADOR}), " +
                    "('Rodrigo Saavedra', 'supervisor@rinconsur.cl', '123456', ${Rol.ID_JEFE_TIENDA}), " +
                    "('Administrador IT', 'admin@rinconsur.cl', '123456', ${Rol.ID_ADMINISTRADOR})"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Activa el chequeo estricto de claves foráneas
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
    }

    // (Opcional) Método de validación directa
    fun validarLogin(correo: String, contrasena: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLA_USUARIOS WHERE $USU_CORREO = ? AND $USU_CONTRASENA = ?",
            arrayOf(correo, contrasena)
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    // ¡NUEVO! Método para buscar un usuario completo por su correo
    fun buscarPorCorreo(correo: String): Usuario? {
        val db = this.readableDatabase
        // Usamos INNER JOIN para traer el nombre del rol además de los datos del usuario
        val cursor = db.rawQuery(
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE " +
                    "FROM $TABLA_USUARIOS u " +
                    "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID " +
                    "WHERE u.$USU_CORREO = ?",
            arrayOf(correo)
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
}