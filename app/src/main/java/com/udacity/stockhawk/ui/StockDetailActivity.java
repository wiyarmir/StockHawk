package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SYMBOL = "extra:symbol";

    @BindView(R.id.text)
    TextView text;

    @BindView(R.id.chart)
    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        String symbol = getIntent().getStringExtra(EXTRA_SYMBOL);

        showHistory(symbol);
    }

    private void showHistory(String symbol) {
        String history = getHistoryString(symbol);

        List<String[]> lines = getLines(history);

        ArrayList<Entry> entries = new ArrayList<>(lines.size());

        final ArrayList<Long> xAxisValues = new ArrayList<>();
        int xAxisPosition = 0;

        for (int i = lines.size() - 1; i >= 0; i--) {
            String[] line = lines.get(i);

            // setup xAxis
            xAxisValues.add(Long.valueOf(line[0]));
            xAxisPosition++;

            // add entry data
            Entry entry = new Entry(
                xAxisPosition, // timestamp
                Float.valueOf(line[1])  // symbol value
            );
            entries.add(entry);
        }

        setupChart(symbol, entries, xAxisValues);

        text.setText(symbol);
    }

    private void setupChart(String symbol, List<Entry> entries, final List<Long> xAxisValues) {
        LineData lineData = new LineData(new LineDataSet(entries, symbol));
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date(xAxisValues.get((int) value));
                return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    .format(date);
            }
        });
    }

    @Nullable
    private List<String[]> getLines(String history) {
        List<String[]> lines = null;
        CSVReader reader = new CSVReader(new StringReader(history));
        try {
            lines = reader.readAll();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private String getHistoryString(String symbol) {
        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol),
            null,
            null,
            null,
            null);
        String history = "";
        if (cursor.moveToFirst()) {
            history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            cursor.close();
        }
        return history;
    }
}
