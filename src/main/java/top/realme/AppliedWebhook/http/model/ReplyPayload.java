package top.realme.AppliedWebhook.http.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ReplyPayload<T>{
    public int code;
    public String message;
    public T data;

    public static <T> ReplyPayload<T> Success(T data) {
        ReplyPayload<T> reply = new ReplyPayload<>();
        reply.code = 200;
        reply.message = "Success";
        reply.data = data;
        return reply;
    }


    public static <T> ReplyPayload<T> Error( String message , int code) {
        var reply = new ReplyPayload<T>();
        reply.code = code;
        reply.message = message;
        reply.data = null;
        return reply;
    }

    public static <T> ReplyPayload<T> NotFound() {
        return Error("Not Found", 404);
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this,new TypeToken<ReplyPayload<T>>(){}.getType());
    }
}
