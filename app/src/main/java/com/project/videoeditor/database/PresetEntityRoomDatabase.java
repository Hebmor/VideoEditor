package com.project.videoeditor.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {PresetEntity.class}, version = 1, exportSchema = false)
public abstract class PresetEntityRoomDatabase extends RoomDatabase {


    public abstract PresetEntityDao presetDao();
    private static volatile PresetEntityRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static PresetEntityRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PresetEntityRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PresetEntityRoomDatabase.class, "app_database")
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
                PresetEntityDao dao = INSTANCE.presetDao();
                dao.deleteAll();

                dao.insert(new PresetEntity("QVGA","320×240","4:3",0.3f,0.15f, 0.3f));
                dao.insert(new PresetEntity("HVGA","320×480","2:3",0.3f,0.15f, 0.3f));
                dao.insert(new PresetEntity("nHD","640×360","16:9",0.75f,0.5f,0.75f));
                dao.insert(new PresetEntity("SVGA","640×480","4:3",1f,0.5f,1f));
                dao.insert(new PresetEntity("FWVGA","800×600","4:3",1.5f,1f,3f));
                dao.insert(new PresetEntity("qHD","960×540","16:9",3f,2.5f,5f));
                dao.insert(new PresetEntity("XGA","1024×768","4:3",3f,2.5f,5f));
                dao.insert(new PresetEntity("XGA+","1152×864","4:3",3f,2.5f,5f));
                dao.insert(new PresetEntity("WXVGA","1200×600","2:1",3f,2.5f,5f));
                dao.insert(new PresetEntity("HD 720p","1280×720","16:9",3f,2.5f,5f));
                dao.insert(new PresetEntity("WXGA","1280×768","5:3",3f,2.5f,5f));
                dao.insert(new PresetEntity("SXGA","1280×1024","5:4",3f,2.5f,5f));
                dao.insert(new PresetEntity("WXGA","1360×768","16:9",3f,2.5f,5f));
                dao.insert(new PresetEntity("WXGA+","1440×900","8:5",3f,2.5f,5f));
                dao.insert(new PresetEntity("SXGA+","1400×1050","4:3",4f,3f,6f));
                dao.insert(new PresetEntity("WXGA++","1600×900","16:9",4f,3f,6f));
                dao.insert(new PresetEntity("WSXGA","1600×1024","25:16",5f,3f,8f));
                dao.insert(new PresetEntity("UXGA","1600×1200","4:3",5f,3f,8f));
                dao.insert(new PresetEntity("WSXGA+","1680×1050","8:5",5f,3f,8f));
                dao.insert(new PresetEntity("Full HD 1080p","1920×1080","16:9",8f,3f,10f));
                dao.insert(new PresetEntity("WUXGA","1920×1200","8:5",8f,3f,10f));
                dao.insert(new PresetEntity("2K","2048×1080","256:135",8f,4f,10f));
                dao.insert(new PresetEntity("QWXGA","2048×1152","16:9",9f,6f,14f));
                dao.insert(new PresetEntity("QXGA","2048×1536","4:3",10f,8f,16f));
                dao.insert(new PresetEntity("Quad HD 1440p","2560×1440","16:9",14f,12f,20f));
                dao.insert(new PresetEntity("WQXGA","2560×1600","8:5",20f,18f,26f));
                dao.insert(new PresetEntity("QHD","3440×1440","43:18",33f,20f,30f));
                dao.insert(new PresetEntity("4K UHD (Ultra HD) 2160p","3840×2160","16:9",66f,44f,85f));

            });
        }

    };
}
