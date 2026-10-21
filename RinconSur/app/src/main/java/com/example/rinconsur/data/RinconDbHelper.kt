package com.example.rinconsur // Cambia esto si lo metiste en una carpeta 'database'

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.rinconsur.model.Rol

class RinconDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "rinconsur.db"
        // Subimos a versión 2 para forzar a que se borre la BD mala y se cree la buena
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

        // Query: Crear tabla Usuarios (Ahora incluye la contraseña)
        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                    "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USU_NOMBRE TEXT NOT NULL, " +
                    "$USU_CORREO TEXT NOT NULL UNIQUE, " +
                    "$USU_CONTRASENA TEXT NOT NULL, " + // <-- COLUMNA NUEVA
                    "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        // Query: Precargar Roles
        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                    "(${Rol.ID_OBSERVADOR}, 'Observador de Pasillo'), " +
                    "(${Rol.ID_JEFE_TIENDA}, 'Jefe de Tienda'), " +
                    "(${Rol.ID_ADMINISTRADOR}, 'Administrador')"

        // Query: Precargar Usuarios (Ahora se inserta '123456' para todos)
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
        // Borra las tablas viejas y las vuelve a crear con las columnas nuevas
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
    }

    // Método que usa el LoginViewModel para validar en la BD
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
}