package com.project.videoeditor.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ExpansionEntity.class}, version = 1, exportSchema = false)
public abstract class ExpansionEntityRoomDatabase extends RoomDatabase {


    public abstract ExpansionEntityDao expansionDao();
    private static volatile ExpansionEntityRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static ExpansionEntityRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ExpansionEntityRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ExpansionEntityRoomDatabase.class, "app_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback()
    {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                ExpansionEntityDao dao = INSTANCE.expansionDao();
                dao.deleteAll();

                dao.insert(new ExpansionEntity("QVGA","320×240","4:3"));
                dao.insert(new ExpansionEntity("HVGA","320×480","2:3"));
                dao.insert(new ExpansionEntity("nHD","640×360","16:9"));
                dao.insert(new ExpansionEntity("SVGA","640×480","4:3"));
                dao.insert(new ExpansionEntity("FWVGA","800×600","4:3"));
                dao.insert(new ExpansionEntity("qHD","960×540","16:9"));
                dao.insert(new ExpansionEntity("XGA","1024×768","4:3"));
                dao.insert(new ExpansionEntity("XGA+","1152×864","4:3"));
                dao.insert(new ExpansionEntity("WXVGA","1200×600","2:1"));
                dao.insert(new ExpansionEntity("HD 720p","1280×720","16:9"));
                dao.insert(new ExpansionEntity("WXGA","1280×768","5:3"));
                dao.insert(new ExpansionEntity("SXGA","1280×1024","5:4"));
                dao.insert(new ExpansionEntity("WXGA","1360×768","16:9"));
                dao.insert(new ExpansionEntity("WXGA+","1440×900","8:5"));
                dao.insert(new ExpansionEntity("SXGA+","1400×1050","4:3"));
                dao.insert(new ExpansionEntity("WXGA++","1600×900","16:9"));
                dao.insert(new ExpansionEntity("WSXGA","1600×1024","25:16"));
                dao.insert(new ExpansionEntity("UXGA","1600×1200","4:3"));
                dao.insert(new ExpansionEntity("WSXGA+","1680×1050","8:5"));
                dao.insert(new ExpansionEntity("Full HD 1080p","1920×1080","16:9"));
                dao.insert(new ExpansionEntity("WUXGA","1920×1200","8:5"));
                dao.insert(new ExpansionEntity("2K","2048×1080","256:135"));
                dao.insert(new ExpansionEntity("QWXGA","2048×1152","16:9"));
                dao.insert(new ExpansionEntity("QXGA","2048×1536","4:3"));
                dao.insert(new ExpansionEntity("Quad HD 1440p","2560×1440","16:9"));
                dao.insert(new ExpansionEntity("WQXGA","2560×1600","8:5"));
                dao.insert(new ExpansionEntity("QHD","3440×1440","43:18"));
                dao.insert(new ExpansionEntity("4K UHD (Ultra HD) 2160p","3840×2160","16:9"));

            });
        }

    };
}
