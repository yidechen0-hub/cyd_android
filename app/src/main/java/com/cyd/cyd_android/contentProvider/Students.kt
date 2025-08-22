package com.cyd.cyd_android.contentProvider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import java.sql.SQLException

class Students : ContentProvider() {
    companion object {
        const val PROVIDER_NAME = "com.cyd.demo"
        const val URL = "content://$PROVIDER_NAME/students"
        val CONTENT_URI: Uri = Uri.parse(URL)

        const val _ID = "_id"
        const val NAME = "name"

        private val STUDENTS_PROJECTION_MAP: HashMap<String, String> = HashMap()

        const val STUDENTS = 1
        const val STUDENT_ID = 2

        val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(PROVIDER_NAME, "students", STUDENTS)
            addURI(PROVIDER_NAME, "students/#", STUDENT_ID)
        }

        // 数据库相关常量
        const val DATABASE_NAME = "College"
        const val STUDENTS_TABLE_NAME = "students"
        const val DATABASE_VERSION = 1
        val CREATE_DB_TABLE = """
            CREATE TABLE $STUDENTS_TABLE_NAME (
                $_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $NAME TEXT NOT NULL
            )
        """.trimIndent()
    }

    private var db: SQLiteDatabase? = null

    private inner class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $STUDENTS_TABLE_NAME")
            onCreate(db)
        }
    }

    override fun onCreate(): Boolean {
        val context = context ?: return false
        val dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
        return db != null
    }

    override fun query(
        uri: Uri,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val qb = SQLiteQueryBuilder().apply {
            tables = STUDENTS_TABLE_NAME
        }

        when (uriMatcher.match(uri)) {
            STUDENTS -> qb.projectionMap = STUDENTS_PROJECTION_MAP
            STUDENT_ID -> qb.appendWhere("$_ID = ${uri.pathSegments[1]}")
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        val order = sortOrder.takeIf { !it.isNullOrEmpty() } ?: NAME
        val cursor = qb.query(db, projection, selection, selectionArgs, null, null, order)

        // 注册内容URI变化监听器
        cursor?.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }


    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            STUDENTS -> "vnd.android.cursor.dir/vnd.example.students"
            STUDENT_ID -> "vnd.android.cursor.item/vnd.example.students"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }



    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // 插入新记录
        val rowID = db?.insert(STUDENTS_TABLE_NAME, "", values) ?: -1

        // 插入成功处理
        if (rowID > 0) {
            val insertedUri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context?.contentResolver?.notifyChange(insertedUri, null)
            return insertedUri
        }
        throw SQLException("Failed to add a record into $uri")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val count = when (uriMatcher.match(uri)) {
            STUDENTS -> db?.delete(STUDENTS_TABLE_NAME, selection, selectionArgs) ?: 0
            STUDENT_ID -> {
                val id = uri.pathSegments[1]
                val whereClause = "$_ID = $id" +
                        (if (!TextUtils.isEmpty(selection)) " AND ($selection)" else "")
                db?.delete(STUDENTS_TABLE_NAME, whereClause, selectionArgs) ?: 0
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String?>?
    ): Int {
        val count = when (uriMatcher.match(uri)) {
            STUDENTS -> db?.update(STUDENTS_TABLE_NAME, values, selection, selectionArgs) ?: 0
            STUDENT_ID -> {
                val id = uri.pathSegments[1]
                val whereClause = "$_ID = $id" +
                        (if (!TextUtils.isEmpty(selection)) " AND ($selection)" else "")
                db?.update(STUDENTS_TABLE_NAME, values, whereClause, selectionArgs) ?: 0
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        context?.contentResolver?.notifyChange(uri, null)
        return count
    }
}