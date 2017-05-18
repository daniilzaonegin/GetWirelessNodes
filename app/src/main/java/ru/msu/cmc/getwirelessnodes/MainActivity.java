package ru.msu.cmc.getwirelessnodes;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

import ru.msu.cmc.getwirelessnodes.Settings.SettingsActivity;

public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);
        if(getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {
            //в случае если портретная ориентация отображаем один фрагмент, который помещается в fragmentContainer
            FragmentManager fm = getSupportFragmentManager();

            Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
            if (fragment == null) {
                fragment = new NodesFragment();
                fm.beginTransaction()
                        .add(R.id.fragmentContainer, fragment)
                        .commit();
            }
        }
        else
        {
            //в случае если портретная ориентация отображаем два фрагмента и используется макет с fragmentContainer_1 и fragmentContainer_2
            FragmentManager fm = getSupportFragmentManager();

            Fragment firstFragment = fm.findFragmentById(R.id.fragmentContainer_1);
            if (firstFragment == null) {
                firstFragment = new NodesFragment();
                fm.beginTransaction()
                        .add(R.id.fragmentContainer_1, firstFragment)
                        .commit();
            }
            Fragment secondFragment = fm.findFragmentById(R.id.fragmentContainer_2);
            if (secondFragment == null) {
                secondFragment = new NodesDetailsFragment();
                fm.beginTransaction()
                        .add(R.id.fragmentContainer_2, secondFragment)
                        .commit();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.fragment_activity,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Intent i = new Intent(this,SettingsActivity.class);
                this.startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // в случае если есть запрос прав, передать его дочерним фрагментам
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
 //   @Override
//   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

//       super.onCreateOptionsMenu(menu, inflater);

//       inflater.inflate(R.menu.fragment_activity,menu);
//   }
}
