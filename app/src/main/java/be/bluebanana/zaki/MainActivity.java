package be.bluebanana.zaki;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //NumbersViewModel model = new ViewModelProvider(this).get(NumbersViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.settings_menu_item) {
            showSettingsScreen();
            return true;
        }
        if (item.getItemId() == R.id.about_menu_item) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void showSettingsScreen () {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}