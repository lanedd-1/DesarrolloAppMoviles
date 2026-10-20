package com.example.rinconsur.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario

class RinconDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "rinconsur.db"
        private const val VERSION_BD = 1

        // Tabla de Roles
        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "id_rol"
        private const val ROL_NOMBRE = "nombre_rol"

        // Tabla de Usuarios
        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_ROL = "id_rol"

        // Creación de tablas
        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                    "$ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$ROL_NOMBRE TEXT NOT NULL UNIQUE)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                    "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USU_NOMBRE TEXT NOT NULL, " +
                    "$USU_CORREO TEXT NOT NULL UNIQUE, " +
                    "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        // Datos Iniciales (Precarga)
        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                    "(${Rol.ID_OBSERVADOR}, 'Observador de Pasillo'), " +
                    "(${Rol.ID_JEFE_TIENDA}, 'Jefe de Tienda'), " +
                    "(${Rol.ID_ADMINISTRADOR}, 'Administrador')"

        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS ($USU_NOMBRE, $USU_CORREO, $USU_ROL) VALUES " +
                    "('Tamara Ramírez', 'observador@rinconsur.cl', ${Rol.ID_OBSERVADOR}), " +
                    "('Rodrigo Saavedra', 'supervisor@rinconsur.cl', ${Rol.ID_JEFE_TIENDA}), " +
                    "('Administrador IT', 'admin@rinconsur.cl', ${Rol.ID_ADMINISTRADOR})"

        // Query Base con INNER JOIN para traer al Usuario con el Nombre de su Rol
        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE " +
                    "FROM $TABLA_USUARIOS u " +
                    "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Activa el chequeo estricto de claves foráneas (Foreign Keys)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En versión 1, aún no se requiere migración estructural (como el Avatar del profe).
        // Se deja preparado para el futuro.
    }

    // =========================================================================
    // MÉTODOS CRUD (Lógica Base de Datos)
    // =========================================================================

    fun listarRoles(): List<Rol> {
        val roles = mutableListOf<Rol>()
        readableDatabase.rawQuery(
            "SELECT $ROL_ID, $ROL_NOMBRE FROM $TABLA_ROLES ORDER BY $ROL_ID", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                roles.add(
                    Rol(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(ROL_ID)),
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE))
                    )
                )
            }
        }
        return roles
    }

    // Retorna el ID generado, o -1 si falla (ej. correo duplicado)
    fun insertarUsuario(usuario: Usuario): Long =
        writableDatabase.insert(TABLA_USUARIOS, null, valoresDe(usuario))

    fun listarUsuarios(): List<Usuario> =
        consultarUsuarios("$SQL_SELECT_USUARIOS ORDER BY u.$USU_NOMBRE", null)

    fun buscarUsuarios(texto: String): List<Usuario> {
        val patron = "%$texto%"
        return consultarUsuarios(
            "$SQL_SELECT_USUARIOS WHERE u.$USU_NOMBRE LIKE ? OR u.$USU_CORREO LIKE ? " +
                    "ORDER BY u.$USU_NOMBRE",
            arrayOf(patron, patron)
        )
    }

    fun buscarPorId(id: Long): Usuario? =
        consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString()))
            .firstOrNull()

    // MÉTODO PARA LOGIN: Busca si el correo existe
    fun buscarPorCorreo(correo: String): Usuario? =
        consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_CORREO = ?", arrayOf(correo))
            .firstOrNull()

    fun actualizarUsuario(usuario: Usuario): Int =
        writableDatabase.update(
            TABLA_USUARIOS, valoresDe(usuario), "$USU_ID = ?", arrayOf(usuario.id.toString())
        )

    fun eliminarUsuario(id: Long): Int =
        writableDatabase.delete(TABLA_USUARIOS, "$USU_ID = ?", arrayOf(id.toString()))

    // =========================================================================
    // FUNCIONES AUXILIARES DE MAPEO
    // =========================================================================

    private fun valoresDe(usuario: Usuario) = ContentValues().apply {
        put(USU_NOMBRE, usuario.nombre)
        put(USU_CORREO, usuario.correo)
        put(USU_ROL, usuario.rolId)
    }

    private fun consultarUsuarios(sql: String, args: Array<String>?): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerUsuario(cursor))
            }
        }
        return lista
    }

    private fun leerUsuario(cursor: Cursor) = Usuario(
        nombre = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
        correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
        id = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ID)),
        rolId = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
        rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE))
    )
}