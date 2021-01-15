package com.vishalperipherals.maps_demo.Network;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class ApplicationRequest extends Application {

    public static final String TAG = com.vishalperipherals.maps_demo.Network.ApplicationRequest.class.getSimpleName();
    private static com.vishalperipherals.maps_demo.Network.ApplicationRequest instance;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private Context context;


    /**
     * Param @ Current application context
     * Create object of Request queue
     * and Image Loader
     * */
    private ApplicationRequest(Context ctx){
        context = ctx;
        requestQueue = getRequestQueue();
        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });

    }

    /**
     * Param @ Application context
     * return Application queue reference
     * */
    public static synchronized com.vishalperipherals.maps_demo.Network.ApplicationRequest getInstance(Context context) {
        if (instance == null) {
            instance = new com.vishalperipherals.maps_demo.Network.ApplicationRequest(context);
        }
        return instance;

    }

    /**
     * Return request queue object
     * */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * param @ request string for http request
     * */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    /**
     * get Image loader request
     * */
    public ImageLoader getImageLoader(){
        return imageLoader;
    }

    /**
     * Cancel all http request
     * */
    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }
}