/*************************************************************************

 Copyright 2025 爅峫

 This file is part of KeyFinder.

 KeyFinder is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 KeyFinder is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with KeyFinder.  If not, see <http://www.gnu.org/licenses/>.

 *************************************************************************/
package moye.sine.task.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moye.sine.task.model.TodoItem;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "todo.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_TODO = "todos";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_COMPLETED = "completed";
    private static final String COLUMN_PARENT_ID = "parent_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_TODO + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_TIME + " BIGINT,"
                + COLUMN_COMPLETED + " INTEGER DEFAULT 0,"
                + COLUMN_PARENT_ID + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_TODO + " ADD COLUMN " + COLUMN_PARENT_ID + " INTEGER DEFAULT 0");
        }
    }

    public List<TodoItem> getAllTodos() {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<Long, TodoItem> itemMap = new HashMap<>();
        List<TodoItem> result = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TODO, null);
        while (cursor.moveToNext()) {
            TodoItem item = new TodoItem(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getLong(2),
                    cursor.getInt(3) == 1,
                    cursor.getLong(4)
            );
            itemMap.put(item.getId(), item);
        }
        cursor.close();

        for (TodoItem item : itemMap.values()) {
            if (item.getParentId() == 0) {
                result.add(item);
            } else {
                TodoItem parent = itemMap.get(item.getParentId());
                if (parent != null) {
                    parent.addChild(item);
                }
            }
        }
        return result;
    }

    public long addTodo(String content, long parentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_TIME, System.currentTimeMillis());
        values.put(COLUMN_PARENT_ID, parentId);
        return db.insert(TABLE_TODO, null, values);
    }

    public boolean deleteTodo(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        List<Long> childrenIds = new ArrayList<>();
        getChildrenIds(id, childrenIds);

        String[] idsArray = new String[childrenIds.size() + 1];
        idsArray[0] = String.valueOf(id);
        for (int i = 0; i < childrenIds.size(); i++) {
            idsArray[i + 1] = String.valueOf(childrenIds.get(i));
        }

        String placeholders = String.join(",", Collections.nCopies(idsArray.length, "?"));
        return db.delete(TABLE_TODO, COLUMN_ID + " IN (" + placeholders + ")", idsArray) > 0;
    }

    private void getChildrenIds(long parentId, List<Long> result) {
        Cursor cursor = getReadableDatabase().query(
                TABLE_TODO,
                new String[]{COLUMN_ID},
                COLUMN_PARENT_ID + " = ?",
                new String[]{String.valueOf(parentId)},
                null, null, null
        );

        while (cursor.moveToNext()) {
            long childId = cursor.getLong(0);
            result.add(childId);
            getChildrenIds(childId, result);
        }
        cursor.close();
    }

    public void updateTodo(TodoItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETED, item.isCompleted() ? 1 : 0);
        db.update(TABLE_TODO, values, COLUMN_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    public void updateTodoContent(TodoItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTENT, item.getContent()); // 更新内容字段
        db.update(TABLE_TODO, values, COLUMN_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }
}