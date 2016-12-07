package com.example.kulker.bazy;

import android.graphics.LayerRasterizer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database;
    RelativeLayout main;
    MapView mapView;
    //com.google.android.gms.maps.MapView mapView;
    ShapefileFeatureTable shapefileFeatureTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        main = (RelativeLayout)findViewById(R.id.activity_main);
        mapView = (MapView)main.findViewById(R.id.map);
        String shapeFilePath = "/storage/emulated/0/budynki.shp";

        File shapefile = new File(shapeFilePath);


        try {
            shapefileFeatureTable = new ShapefileFeatureTable(shapefile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Toast.makeText(this, "ShapeFile Not Found!!!", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
            return;
        }


        FeatureLayer featureLayer = new FeatureLayer(shapefileFeatureTable);
        featureLayer.setRenderer(new SimpleRenderer(new SimpleFillSymbol(
               getResources().getColor(android.R.color.holo_blue_bright),
                SimpleFillSymbol.STYLE.SOLID)));
        mapView.addLayer(featureLayer);


    }


}
