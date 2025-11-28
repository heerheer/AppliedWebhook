package top.realme.AppliedWebhook.ae.serialize;

import appeng.api.stacks.AEKey;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;

public class AEKeySerializer implements ObjectWriter<AEKey> {

    public static final AEKeySerializer INSTANCE = new AEKeySerializer();

    @Override
    public void write(JSONWriter writer, Object key, Object fieldName, Type fieldType, long features) {
        // 你可以自定义想导出的字段
        AEKey aekey = (AEKey) key;
        writer.writeString(aekey.toString());
    }
}

