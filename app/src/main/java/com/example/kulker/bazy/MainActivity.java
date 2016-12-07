package com.example.kulker.bazy;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database;
    RelativeLayout main;
    MapView mapView;
    //com.google.android.gms.maps.MapView mapView;
    ShapefileFeatureTable shapefileFeatureTable;
    MainActivity t = this;
    SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(Color.RED, 1000, SimpleMarkerSymbol.STYLE.CIRCLE);
    GraphicsLayer graphicsLayer = new GraphicsLayer();
    FeatureLayer featureLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        main = (RelativeLayout)findViewById(R.id.activity_main);
        mapView = (MapView)main.findViewById(R.id.map);
        String shapeFilePath = Environment.getExternalStorageDirectory() + "/budynki.shp";
        try {
            File shapefile = new File(shapeFilePath);
            shapefileFeatureTable = new ShapefileFeatureTable(shapefile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Toast.makeText(this, "ShapeFile Not Found!!!", LENGTH_LONG).show();
            ex.printStackTrace();
            return;
        }
        featureLayer = new FeatureLayer(shapefileFeatureTable);
        featureLayer.setRenderer(new SimpleRenderer(new SimpleFillSymbol(
               getResources().getColor(android.R.color.holo_blue_bright),
                SimpleFillSymbol.STYLE.SOLID)));
        graphicsLayer.setRenderer(new SimpleRenderer(new SimpleFillSymbol(
                getResources().getColor(android.R.color.holo_red_dark),
                SimpleFillSymbol.STYLE.SOLID)));
        mapView.addLayer(featureLayer);
        mapView.addLayer(graphicsLayer);

        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float v, float v1) {
                String text = "v: "+v+" , v1: "+v1;
                //Toast.makeText(t, text , LENGTH_LONG).show();
                Point graphicPoint = new Point(v, v1);
                Graphic graphic = new Graphic(graphicPoint, symbol);
                graphicsLayer.addGraphic(graphic);

                mapView.addLayer(featureLayer);
                mapView.addLayer(graphicsLayer);
            }
        });
    }


}
