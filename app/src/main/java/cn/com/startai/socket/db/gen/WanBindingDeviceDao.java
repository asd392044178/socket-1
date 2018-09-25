package cn.com.startai.socket.db.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import cn.com.startai.socket.sign.hardware.WiFi.bean.WanBindingDevice;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "WAN_BINDING_DEVICE".
*/
public class WanBindingDeviceDao extends AbstractDao<WanBindingDevice, Long> {

    public static final String TABLENAME = "WAN_BINDING_DEVICE";

    /**
     * Properties of entity WanBindingDevice.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Gid = new Property(0, Long.class, "gid", true, "_id");
        public final static Property Apptype = new Property(1, String.class, "apptype", false, "APPTYPE");
        public final static Property Bindingtime = new Property(2, long.class, "bindingtime", false, "BINDINGTIME");
        public final static Property Connstatus = new Property(3, int.class, "connstatus", false, "CONNSTATUS");
        public final static Property Oid = new Property(4, String.class, "oid", false, "OID");
        public final static Property Type = new Property(5, int.class, "type", false, "TYPE");
        public final static Property Alias = new Property(6, String.class, "alias", false, "ALIAS");
        public final static Property Topic = new Property(7, String.class, "topic", false, "TOPIC");
        public final static Property Mac = new Property(8, String.class, "mac", false, "MAC");
        public final static Property Mid = new Property(9, String.class, "mid", false, "MID");
        public final static Property IsAdmin = new Property(10, boolean.class, "isAdmin", false, "IS_ADMIN");
        public final static Property HasBindingByWan = new Property(11, boolean.class, "hasBindingByWan", false, "HAS_BINDING_BY_WAN");
        public final static Property HasBindingByLan = new Property(12, boolean.class, "hasBindingByLan", false, "HAS_BINDING_BY_LAN");
        public final static Property CpuInfo = new Property(13, String.class, "cpuInfo", false, "CPU_INFO");
        public final static Property Token = new Property(14, int.class, "token", false, "TOKEN");
        public final static Property TokenInsterTimes = new Property(15, long.class, "tokenInsterTimes", false, "TOKEN_INSTER_TIMES");
    }


    public WanBindingDeviceDao(DaoConfig config) {
        super(config);
    }
    
    public WanBindingDeviceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"WAN_BINDING_DEVICE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: gid
                "\"APPTYPE\" TEXT," + // 1: apptype
                "\"BINDINGTIME\" INTEGER NOT NULL ," + // 2: bindingtime
                "\"CONNSTATUS\" INTEGER NOT NULL ," + // 3: connstatus
                "\"OID\" TEXT," + // 4: oid
                "\"TYPE\" INTEGER NOT NULL ," + // 5: type
                "\"ALIAS\" TEXT," + // 6: alias
                "\"TOPIC\" TEXT," + // 7: topic
                "\"MAC\" TEXT," + // 8: mac
                "\"MID\" TEXT," + // 9: mid
                "\"IS_ADMIN\" INTEGER NOT NULL ," + // 10: isAdmin
                "\"HAS_BINDING_BY_WAN\" INTEGER NOT NULL ," + // 11: hasBindingByWan
                "\"HAS_BINDING_BY_LAN\" INTEGER NOT NULL ," + // 12: hasBindingByLan
                "\"CPU_INFO\" TEXT," + // 13: cpuInfo
                "\"TOKEN\" INTEGER NOT NULL ," + // 14: token
                "\"TOKEN_INSTER_TIMES\" INTEGER NOT NULL );"); // 15: tokenInsterTimes
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"WAN_BINDING_DEVICE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, WanBindingDevice entity) {
        stmt.clearBindings();
 
        Long gid = entity.getGid();
        if (gid != null) {
            stmt.bindLong(1, gid);
        }
 
        String apptype = entity.getApptype();
        if (apptype != null) {
            stmt.bindString(2, apptype);
        }
        stmt.bindLong(3, entity.getBindingtime());
        stmt.bindLong(4, entity.getConnstatus());
 
        String oid = entity.getOid();
        if (oid != null) {
            stmt.bindString(5, oid);
        }
        stmt.bindLong(6, entity.getType());
 
        String alias = entity.getAlias();
        if (alias != null) {
            stmt.bindString(7, alias);
        }
 
        String topic = entity.getTopic();
        if (topic != null) {
            stmt.bindString(8, topic);
        }
 
        String mac = entity.getMac();
        if (mac != null) {
            stmt.bindString(9, mac);
        }
 
        String mid = entity.getMid();
        if (mid != null) {
            stmt.bindString(10, mid);
        }
        stmt.bindLong(11, entity.getIsAdmin() ? 1L: 0L);
        stmt.bindLong(12, entity.getHasBindingByWan() ? 1L: 0L);
        stmt.bindLong(13, entity.getHasBindingByLan() ? 1L: 0L);
 
        String cpuInfo = entity.getCpuInfo();
        if (cpuInfo != null) {
            stmt.bindString(14, cpuInfo);
        }
        stmt.bindLong(15, entity.getToken());
        stmt.bindLong(16, entity.getTokenInsterTimes());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, WanBindingDevice entity) {
        stmt.clearBindings();
 
        Long gid = entity.getGid();
        if (gid != null) {
            stmt.bindLong(1, gid);
        }
 
        String apptype = entity.getApptype();
        if (apptype != null) {
            stmt.bindString(2, apptype);
        }
        stmt.bindLong(3, entity.getBindingtime());
        stmt.bindLong(4, entity.getConnstatus());
 
        String oid = entity.getOid();
        if (oid != null) {
            stmt.bindString(5, oid);
        }
        stmt.bindLong(6, entity.getType());
 
        String alias = entity.getAlias();
        if (alias != null) {
            stmt.bindString(7, alias);
        }
 
        String topic = entity.getTopic();
        if (topic != null) {
            stmt.bindString(8, topic);
        }
 
        String mac = entity.getMac();
        if (mac != null) {
            stmt.bindString(9, mac);
        }
 
        String mid = entity.getMid();
        if (mid != null) {
            stmt.bindString(10, mid);
        }
        stmt.bindLong(11, entity.getIsAdmin() ? 1L: 0L);
        stmt.bindLong(12, entity.getHasBindingByWan() ? 1L: 0L);
        stmt.bindLong(13, entity.getHasBindingByLan() ? 1L: 0L);
 
        String cpuInfo = entity.getCpuInfo();
        if (cpuInfo != null) {
            stmt.bindString(14, cpuInfo);
        }
        stmt.bindLong(15, entity.getToken());
        stmt.bindLong(16, entity.getTokenInsterTimes());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public WanBindingDevice readEntity(Cursor cursor, int offset) {
        WanBindingDevice entity = new WanBindingDevice( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // gid
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // apptype
            cursor.getLong(offset + 2), // bindingtime
            cursor.getInt(offset + 3), // connstatus
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // oid
            cursor.getInt(offset + 5), // type
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // alias
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // topic
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // mac
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // mid
            cursor.getShort(offset + 10) != 0, // isAdmin
            cursor.getShort(offset + 11) != 0, // hasBindingByWan
            cursor.getShort(offset + 12) != 0, // hasBindingByLan
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // cpuInfo
            cursor.getInt(offset + 14), // token
            cursor.getLong(offset + 15) // tokenInsterTimes
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, WanBindingDevice entity, int offset) {
        entity.setGid(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setApptype(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setBindingtime(cursor.getLong(offset + 2));
        entity.setConnstatus(cursor.getInt(offset + 3));
        entity.setOid(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setType(cursor.getInt(offset + 5));
        entity.setAlias(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setTopic(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setMac(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setMid(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setIsAdmin(cursor.getShort(offset + 10) != 0);
        entity.setHasBindingByWan(cursor.getShort(offset + 11) != 0);
        entity.setHasBindingByLan(cursor.getShort(offset + 12) != 0);
        entity.setCpuInfo(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setToken(cursor.getInt(offset + 14));
        entity.setTokenInsterTimes(cursor.getLong(offset + 15));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(WanBindingDevice entity, long rowId) {
        entity.setGid(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(WanBindingDevice entity) {
        if(entity != null) {
            return entity.getGid();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(WanBindingDevice entity) {
        return entity.getGid() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
