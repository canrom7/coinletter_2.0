package com.iimm.miliao.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSON;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.util.GoogleMapInfoEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class GoogleMapHelper extends MapHelper {
    private static final String TAG = "GoogleMapHelper";
    @SuppressLint("StaticFieldLeak")
    private static GoogleMapHelper INSTANCE;
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private OkHttpClient okHttpClient;

    private GoogleMapHelper(Context context) {
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public static GoogleMapHelper getInstance(Context context) {
        // 单例用懒加载，为了传入context,
        if (INSTANCE == null) {
            synchronized (GoogleMapHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GoogleMapHelper(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public boolean isAvailability() {
        boolean isGoogleAvailability = true;
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            // 这里拿不到activity，不弹原因了，一般也就是谷歌框架服务需要升级什么的，
/*
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(activity, resultCode, 2404).show();
            }
*/
            isGoogleAvailability = false;
        }
        return isGoogleAvailability;
    }

    @Override
    public String getStaticImage(LatLng latLng) {
        // 谷歌已经禁止不带key访问静态地图了，为此项目里已经不使用静态地图了，代码保留，
        // 要把自己的key填在这里，
        // 然后谷歌平台绑定信用卡能扣费才能用，否则一天只能调用一次，
//        String key = BuildConfig.GOOGLE_API_KEY;
        // 这个是官方测试key, 不保证能用，
        String key = "AIzaSyDIJ9XX2ZvRKCJcFRrl-lRanEtFUow4piM";
        return "http://maps.googleapis.com/maps/api/staticmap?" +
                "center=" + latLng.getLatitude() + "," + latLng.getLongitude() +
                "&size=640x480&markers=color:blue%7Clabel:S%7C62.107733,-145.541936" +
                "&zoom=15&key=" + key;
    }

    @Override
    public void requestLatLng(final OnSuccessListener<LatLng> onSuccessListener, final OnErrorListener onErrorListener) {
        final Task<Location> lastLocation;
        try {
            lastLocation = fusedLocationProviderClient.getLastLocation();
        } catch (SecurityException e) {
            if (onErrorListener != null) {
                onErrorListener.onError(new RuntimeException("没有位置权限,", e));
            }
            return;
        }
        lastLocation.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                // 设备上的google service出意外可能导致这个task成功但是location为空，
                if (!task.isSuccessful() || location == null) {
                    if (onErrorListener != null) {
                        onErrorListener.onError(new RuntimeException("定位失败,", task.getException()));
                    }
                    return;
                }
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(latLng);
                }
            }
        });
    }

    @Override
    public void requestPlaceList(LatLng latLng, final OnSuccessListener<List<Place>> onSuccessListener, final OnErrorListener onErrorListener) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?language=ZH&location=" + latLng.getLatitude() + "," + latLng.getLongitude()
                + "&radius=" + 100 + "&key=" + BuildConfig.GOOGLE_API_KEY;
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder().build();
        }
        Request.Builder requestBuilder = new Request.Builder();
        final Request request = requestBuilder.url(url).build();
        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                if (onErrorListener != null) {
                    onErrorListener.onError(new RuntimeException("获取周边位置失败,", e));
                }
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String msg = response.body().string();
                Log.i(TAG, msg);
                try {
                    JSONObject jsonObject = new JSONObject(msg);
                    String status = jsonObject.getString("status");
                    if (status.equals("OK")) {
                        JSONArray results = jsonObject.getJSONArray("results");
                        List<GoogleMapInfoEntity> googleMapInfoEntityList = JSON.parseArray(results.toString(), GoogleMapInfoEntity.class);
                        List<Place> placeList = new ArrayList<>();
                        for (int i = 0; i < googleMapInfoEntityList.size(); i++) {
                            GoogleMapInfoEntity gPlace = googleMapInfoEntityList.get(i);
                            if (gPlace != null) {
                                if (!TextUtils.isEmpty(gPlace.getName()) && !TextUtils.isEmpty(gPlace.getVicinity()) &&
                                        0.0 != gPlace.getGeometry().getLocation().getLat() && 0.0 != gPlace.getGeometry().getLocation().getLng()) {
                                    String name = gPlace.getName();
                                    // 以防万一避免空指针，
                                    String address = "" + gPlace.getVicinity();
                                    com.google.android.gms.maps.model.LatLng latLng = new com.google.android.gms.maps.model.LatLng(gPlace.getGeometry().getLocation().getLat(),
                                            gPlace.getGeometry().getLocation().getLng());
                                    LatLng placeLatLng = new LatLng(latLng.latitude, latLng.longitude);
                                    Place place = new Place(name, address, placeLatLng);
                                    placeList.add(place);
                                }
                            } else {
                                continue;
                            }

                        }
                        if (onSuccessListener != null) {

                            onSuccessListener.onSuccess(placeList);
                        }
                    } else {
                        onErrorListener.onError(new Throwable(status));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
//        PlaceDetectionClient placeDetectionClient = Places.getPlaceDetectionClient(context);
//        // TODO: 这是获取了当前位置的周边信息，没用到参数的经纬度，
//        @SuppressLint("MissingPermission") Task<PlaceLikelihoodBufferResponse> currentPlace = placeDetectionClient.getCurrentPlace(null);
//        currentPlace.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
//            @Override
//            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
//                if (!task.isSuccessful()) {
//                    if (onErrorListener != null) {
//                        onErrorListener.onError(new RuntimeException("获取周边位置失败,", task.getException()));
//                    }
//                    return;
//                }
//                PlaceLikelihoodBufferResponse placeLikelihoods = task.getResult();
//                if (placeLikelihoods == null) {
//                    if (onErrorListener != null) {
//                        onErrorListener.onError(new RuntimeException("获取周边位置失败,", task.getException()));
//                    }
//                    return;
//                }
//                List<Place> placeList = new ArrayList<>(placeLikelihoods.getCount());
//                for (int i = 0; i < placeLikelihoods.getCount(); i++) {
//                    com.google.android.gms.location.places.Place gPlace = placeLikelihoods.get(i).getPlace();
//                    String name = gPlace.getName().toString();
//                    // 以防万一避免空指针，
//                    String address = "" + gPlace.getAddress();
//                    LatLng placeLatLng = new LatLng(gPlace.getLatLng().latitude, gPlace.getLatLng().longitude);
//                    Place place = new Place(name, address, placeLatLng);
//                    placeList.add(place);
//                }
//                if (onSuccessListener != null) {
//                    onSuccessListener.onSuccess(placeList);
//                }
//            }
//        });
    }

    @Override
    public void requestCityName(LatLng latLng, final OnSuccessListener<String> onSuccessListener, final OnErrorListener onErrorListener) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
        } catch (IOException e) {
            if (onErrorListener != null) {
                onErrorListener.onError(new RuntimeException("获取位置失败,", e));
            }
            return;
        }
        if (addressList == null || addressList.isEmpty()) {
            if (onErrorListener != null) {
                onErrorListener.onError(new RuntimeException("获取位置失败,"));
            }
            return;
        }
        Address address = addressList.get(0);
        if (address == null || address.getLocality() == null) {
            if (onErrorListener != null) {
                onErrorListener.onError(new RuntimeException("获取位置失败,"));
            }
            return;
        }
        if (onSuccessListener != null) {
            onSuccessListener.onSuccess(address.getLocality());
        }
    }

    @Override
    public Picker getPicker(Context context) {
        return new GoogleMapPicker(context);
    }


    private class GoogleMapPicker extends Picker implements OnMapReadyCallback {
        private MapView mapView;
        private Context context;
        private GoogleMap googleMap;
        private OnMapReadyListener onMapReadyListener;
        private com.google.android.gms.maps.model.Marker centerMarker;
        private Bitmap centerBitmap;

        private GoogleMapPicker(Context context) {
            this.context = context;
        }

        private void createMapView() {
            if (mapView == null) {
                mapView = new MapView(context);
                mapView.setClickable(true);
                mapView.setFocusable(true);
            }
        }

        @Override
        public void attack(FrameLayout container, OnMapReadyListener listener) {
            Log.d(TAG, "attack: ");
            this.onMapReadyListener = listener;
            createMapView();
            ViewGroup viewGroup = (ViewGroup) mapView.getParent();
            if (viewGroup != null) {
                viewGroup.removeAllViews();
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(mapView, params);
            mapView.getMapAsync(this);
        }

        @Override
        public MapView getMapView() {
            return mapView;
        }

        @Override
        public void snapshot(final Rect rect, final SnapshotReadyCallback callback) {
            googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    int left = rect.left;
                    int top = rect.top;
                    int rw = rect.right - rect.left;
                    int rh = rect.bottom - rect.top;
                    callback.onSnapshotReady(Bitmap.createBitmap(bitmap, left, top, rw, rh));
                }
            });
        }

        @Override
        public void showMyLocation(LatLng latLng) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(true);
        }

        @Override
        public void addCenterMarker(Bitmap bitmap, @Nullable String info) {
            centerBitmap = bitmap;
            if (googleMap == null) {
                Log.e(TAG, "addMarker: 添加标记失败，", new RuntimeException("谷歌地图还没准备好，"));
                return;
            }
            createMapView();
            com.google.android.gms.maps.model.LatLng gLatLng = googleMap.getCameraPosition().target;
            // 构建Marker图标
            BitmapDescriptor gBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
            centerMarker = googleMap.addMarker(new MarkerOptions()
                    .icon(gBitmap)
                    .position(gLatLng)
                    .title(info));
        }

        @Override
        public void addMarker(LatLng latLng, Bitmap bitmap, @Nullable String info) {
            if (googleMap == null) {
                Log.e(TAG, "addMarker: 添加标记失败，", new RuntimeException("谷歌地图还没准备好，"));
                return;
            }
            createMapView();
            com.google.android.gms.maps.model.LatLng gLatLng = new com.google.android.gms.maps.model.LatLng(latLng.getLatitude(), latLng.getLongitude());
            // 构建Marker图标
            BitmapDescriptor gBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
            googleMap.addMarker(new MarkerOptions()
                    .icon(gBitmap)
                    .position(gLatLng)
                    .title(info));
        }

        @Override
        public void clearMarker() {
            googleMap.clear();
            if (centerMarker != null) {
                addCenterMarker(centerBitmap, centerMarker.getTitle());
            }
        }

        @Override
        public LatLng currentLatLng() {
            CameraPosition cameraPosition = googleMap.getCameraPosition();
            com.google.android.gms.maps.model.LatLng target = cameraPosition.target;
            return new LatLng(target.latitude, target.longitude);
        }

        @Override
        public void moveMap(LatLng latLng, boolean anim) {
            if (googleMap == null) {
                Log.e(TAG, "moveMap: 移动地图失败，", new RuntimeException("谷歌地图还没准备好，"));
                return;
            }
            createMapView();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new com.google.android.gms.maps.model.LatLng(latLng.getLatitude(), latLng.getLongitude()),
                    15f);
            if (anim) {
                googleMap.animateCamera(cameraUpdate);
            } else {
                googleMap.moveCamera(cameraUpdate);
            }
        }

        @Override
        public void onMapReady(final GoogleMap googleMap) {
            Log.d(TAG, "onMapReady() called with: googleMap = [" + googleMap + "]");
            this.googleMap = googleMap;
            googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                @Override
                public void onCameraMoveStarted(int reason) {
                    if (REASON_GESTURE != reason) {
                        // 不是用户拖出来的事件无视，
                        // TODO: 只在这里无视好像没什么用，下面的move和idle还是执行了，
                        Log.d(TAG, "onCameraMoveStarted() called with: i = [" + reason + "]");
                        return;
                    }
                    MapStatus mapStatus = new MapStatus();
                    com.google.android.gms.maps.model.LatLng target = googleMap.getCameraPosition().target;
                    mapStatus.target = new LatLng(target.latitude, target.longitude);
                    if (onMapStatusChangeListener != null) {
                        onMapStatusChangeListener.onMapStatusChangeStart(mapStatus);
                    }
                }
            });
            googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    MapStatus mapStatus = new MapStatus();
                    com.google.android.gms.maps.model.LatLng target = googleMap.getCameraPosition().target;
                    mapStatus.target = new LatLng(target.latitude, target.longitude);
                    if (onMapStatusChangeListener != null) {
                        onMapStatusChangeListener.onMapStatusChange(mapStatus);
                    }
                    if (centerMarker != null) {
                        // 锁定在中心的marker移到中心，
                        centerMarker.setPosition(target);
                    }
                }
            });
            googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    MapStatus mapStatus = new MapStatus();
                    com.google.android.gms.maps.model.LatLng target = googleMap.getCameraPosition().target;
                    mapStatus.target = new LatLng(target.latitude, target.longitude);
                    if (onMapStatusChangeListener != null) {
                        onMapStatusChangeListener.onMapStatusChangeFinish(mapStatus);
                    }
                    if (centerMarker != null) {
                        // 锁定在中心的marker移到中心，
                        centerMarker.setPosition(target);
                    }
                }
            });
/*
            googleMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
                @Override
                public void onCameraMoveCanceled() {
                    MapStatus mapStatus = new MapStatus();
                    com.google.android.gms.maps.model.LatLng target = googleMap.getCameraPosition().target;
                    mapStatus.target = new LatLng(target.latitude, target.longitude);
                    onMapStatusChangeListener.onMapStatusChangeFinish(mapStatus);
                }
            });
*/

            googleMap.setOnMarkerClickListener(markerGoogle -> {
                if (onMarkerClickListener != null) {
                    Marker marker = new Marker();
                    // TODO: 谷歌的marker拿不到bitmap,
                    marker.setLatLng(new LatLng(markerGoogle.getPosition().latitude, markerGoogle.getPosition().longitude));
                    marker.setInfo(markerGoogle.getTitle());
                    onMarkerClickListener.onMarkerClick(marker);
                }
                return true;
            });
            if (onMapReadyListener != null) {
                onMapReadyListener.onMapReady();
            }
        }

        @Override
        void create() {
            super.create();
            createMapView();
            mapView.onCreate(null);
        }

        @Override
        void start() {
            super.start();
            mapView.onStart();
        }

        @Override
        void resume() {
            super.resume();
            mapView.onResume();
        }

        @Override
        void pause() {
            super.pause();
            mapView.onPause();
        }

        @Override
        void stop() {
            super.stop();
            mapView.onStop();
        }

        @Override
        void destroy() {
            super.destroy();
            mapView.onDestroy();
        }
    }

    @Override
    public void isInChina(OnSuccessListener<Boolean> booleanOnSuccessListener, OnErrorListener onErrorListener) {
        booleanOnSuccessListener.onSuccess(true);
    }
}
