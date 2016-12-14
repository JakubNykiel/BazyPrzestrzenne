package com.example.kulker.bazy;

import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.y;

public class MainActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    DatabaseReference currentPoint;
    RelativeLayout main;
    MapView mapView;
    Button start;
    Button stop;
    ShapefileFeatureTable shapefileFeatureTable;
    FeatureLayer featureLayer;
    GraphicsLayer graphicsLayer = new GraphicsLayer();
    TextView pointData;
    TextView signalData;
    WifiManager wifiManager;
    String node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        main = (RelativeLayout)findViewById(R.id.activity_main);
        mapView = (MapView)main.findViewById(R.id.map);
        pointData = (TextView)main.findViewById(R.id.pointData);
        stop = (Button)main.findViewById(R.id.stopBtn);
        signalData = (TextView)main.findViewById(R.id.signalData);
        signalData.setMovementMethod(new ScrollingMovementMethod());
        LoadFile();

        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartFirebaseNode();
                StartButtonListnerMethod();
            }
        });

        featureLayer = new FeatureLayer(shapefileFeatureTable);
        featureLayer.setRenderer(new SimpleRenderer(new SimpleFillSymbol(
                getResources().getColor(android.R.color.holo_blue_bright),
                SimpleFillSymbol.STYLE.SOLID)));

        mapView.addLayer(featureLayer);
        mapView.addLayer(graphicsLayer);
    }

    private void StartButtonListnerMethod(){
        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float v, float v1) {
                AddMarkerToMap(v,v1);
            }
        });
    }

    private void StopButtonListnerMethod(){
        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float v, float v1) {
            }
        });
    }

    private void AddMarkerToMap(float v, float v1){
        SimpleMarkerSymbol simpleMarker = new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.CIRCLE);

        Point pointGeometry = mapView.toMapPoint(v,v1);
        Graphic pointGraphic = new Graphic(pointGeometry, simpleMarker);
        graphicsLayer.addGraphic(pointGraphic);
        PrintPointData(pointGeometry);
    }

    private void LoadFile(){
        String shapeFilePath = Environment.getExternalStorageDirectory() + "/budynki.shp";
        start = (Button)main.findViewById(R.id.startBtn);
        try {
            File shapefile = new File(shapeFilePath);
            shapefileFeatureTable = new ShapefileFeatureTable(shapefile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Toast.makeText(this, "ShapeFile Not Found!!!", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
            return;
        }
    }

    private void PrintPointData(Point point){
        Point p = ConvertPoint(point);
        String tmp = "Lng: "+p.getX()+" Lat: "+p.getY();
        pointData.setText(tmp);
        Map<String,Integer> s = GetSignalStrength();
        AddPointToNode(p,s);
    }

    private Point ConvertPoint(Point point){
        SpatialReference srFrom = SpatialReference.create(2180);
        SpatialReference srTo = SpatialReference.create(4269); //WGS84
        return (Point) GeometryEngine.project(point,srFrom,srTo);
    }

    private Map<String,Integer> GetSignalStrength(){
        List<ScanResult> wifiList = wifiManager.getScanResults();
        Map<String, Integer> strenght = new HashMap<String, Integer>();
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult scanResult = wifiList.get(i);
            strenght.put(scanResult.SSID,scanResult.level);
            signalData.append(scanResult.SSID+":"+scanResult.level+"\n");
        }
        return strenght;
   }

    private void StartFirebaseNode(){
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        Date myDate = new Date();
        node = timeStampFormat.format(myDate);
        mDatabase.child(node).setValue("");
    }

    private void AddPointToNode(Point point, Map<String,Integer> strength){
        currentPoint = mDatabase.child(node).push();
        currentPoint.child("Lat").setValue(point.getY());
        currentPoint.child("Lng").setValue(point.getX());
        currentPoint.child("WiFi").setValue(strength);
    }
}

