package com.hci.lab430.myapplication.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hci.lab430.myapplication.R;
import com.hci.lab430.myapplication.adapter.PokemonSearchListViewAdapter;
import com.hci.lab430.myapplication.model.PokemonType;
import com.hci.lab430.myapplication.model.SearchPokemonInfo;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lab430 on 16/8/11.
 */
public class PokemonSearchFragment extends ItemFragment implements DialogInterface.OnClickListener{

    AlertDialog alertDialog;
    View fragmentView;
    TextView infoText;
    public PokemonSearchListViewAdapter adapter;
    DialogViewHolder dialogViewHolder;

    ArrayList<SearchPokemonInfo> searchResult = new ArrayList<>();
    public ArrayList<String> typeList = null;

    public static PokemonSearchFragment newInstance() {

        Bundle args = new Bundle();

        PokemonSearchFragment fragment = new PokemonSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setMenuVisibility(false);

    }

    public void requestTypeList() {
        PokemonType.getQuery().getFirstInBackground(new GetCallback<PokemonType>() {
            @Override
            public void done(PokemonType object, ParseException e) {
                if (e == null) {
                    typeList = object.getTypeArray();
                    if (typeList != null)
                        setMenuVisibility(true);
                    if (dialogViewHolder != null) {
                        dialogViewHolder.setTypeList(0, typeList);
                        dialogViewHolder.setTypeList(1, typeList);
                    } else {
                        setMenuVisibility(false);
                        Toast.makeText(getActivity(), "沒抓到屬性列表", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void hideOrShowInfoText(ArrayList<SearchPokemonInfo> result) {
        if(infoText != null) {
            if (result.size() == 0) {
                infoText.setVisibility(View.VISIBLE);
            } else {
                infoText.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if(fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.fragment_search, container, false);
            adapter = new PokemonSearchListViewAdapter(getActivity(),
                    R.layout.search_row_view,
                    searchResult,
                    this);
            ((ListView)fragmentView.findViewById(R.id.listView)).setAdapter(adapter);
            infoText = (TextView)fragmentView.findViewById(R.id.infoText);
        }

        hideOrShowInfoText(searchResult);

        if(alertDialog == null) {
            View dialogView = inflater.inflate(R.layout.search_form, null);
            dialogViewHolder = new DialogViewHolder(dialogView);
            if(typeList != null) {
                dialogViewHolder.setTypeList(0, typeList);
                dialogViewHolder.setTypeList(1, typeList);
            }
            alertDialog = new AlertDialog.Builder(getActivity()).setView(dialogView)
                    .setNegativeButton("取消", this)
                    .setPositiveButton("搜尋", this)
                    .create();
        }

        return fragmentView;
    }

    private void startSearching() {
        ParseQuery<SearchPokemonInfo> query = SearchPokemonInfo.getQuery();
        CustomizedFindCallback findCallback = new CustomizedFindCallback(searchResult,this);

        if(dialogViewHolder.conditionIsUsed(0)) { //name box
            query = query.whereContains(SearchPokemonInfo.nameKey, dialogViewHolder.getInputName());
        }

        if(dialogViewHolder.conditionIsUsed(1)) { //hp box
            if(dialogViewHolder.constrainedByLeftInterval()) {
                query = query.whereGreaterThan(SearchPokemonInfo.hpKey, dialogViewHolder.getLeftIntervalVal());
            }

            if(dialogViewHolder.constrainedByRightInterval()) {
                query = query.whereLessThan(SearchPokemonInfo.hpKey, dialogViewHolder.getRightIntervalVal());
            }
            query = query.addAscendingOrder(SearchPokemonInfo.hpKey);
        }

        if(dialogViewHolder.conditionIsUsed(2)) { //types box
            ArrayList<Integer> typesCondition = new ArrayList<>();
            for(int i = 0;i < 2;i++) {
                int selectedType = dialogViewHolder.getSelectedType(i);
                if(selectedType != -1) {
                    typesCondition.add(selectedType);
                }
            }
            findCallback.numTypesInCondition = typesCondition.size();
            query = query.whereContainsAll(SearchPokemonInfo.typesKey,
                    typesCondition);
        }

        query.findInBackground(findCallback);
    }

    private static class CustomizedFindCallback implements FindCallback<SearchPokemonInfo> {

        public int numTypesInCondition = -1;
        private ArrayList<SearchPokemonInfo> searchResult;
        private PokemonSearchFragment searchFragment;
        CustomizedFindCallback(ArrayList<SearchPokemonInfo> resultBuffer, PokemonSearchFragment fragment) {
            searchResult = resultBuffer;
            searchFragment = fragment;
        }

        @Override
        public void done(List<SearchPokemonInfo> objects, ParseException e) {
            searchResult.clear();
            if(numTypesInCondition != -1) {
                for(SearchPokemonInfo searchPokemonInfo : objects) {
                    ArrayList<Integer> typeIndices =
                            searchPokemonInfo.getTypeIndices();
                    if(typeIndices.size() == numTypesInCondition) {
                        searchResult.add(searchPokemonInfo);
                    }
                }
            }
            else {
                searchResult.addAll(objects);
            }

            searchFragment.hideOrShowInfoText(searchResult);
            searchFragment.adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(typeList == null) {
            requestTypeList();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if(which == AlertDialog.BUTTON_POSITIVE) {
            startSearching();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_search) {
            alertDialog.show();
            return true;
        }

        return false;
    }

    public static class DialogViewHolder {
        View dialogView;

        CheckBox[] conditionBoxes = new CheckBox[3];
        CheckBox leftIntervalBox;
        CheckBox rightIntervalBox;

        EditText nameText;
        EditText leftInterval;
        EditText rightInterval;
        Spinner[] typeSelectors = new Spinner[2];

        DialogViewHolder(View dialogView) {
            this.dialogView = dialogView;
            conditionBoxes[0] = (CheckBox)dialogView.findViewById(R.id.conditionBox1);
            conditionBoxes[1] = (CheckBox)dialogView.findViewById(R.id.conditionBox2);
            conditionBoxes[2] = (CheckBox)dialogView.findViewById(R.id.conditionBox3);
            leftIntervalBox = (CheckBox)dialogView.findViewById(R.id.leftIntervalConditionBox);
            rightIntervalBox = (CheckBox)dialogView.findViewById(R.id.rightIntervalConditionBox);

            nameText = (EditText)dialogView.findViewById(R.id.nameText);
            leftInterval = (EditText)dialogView.findViewById(R.id.leftInterval);
            rightInterval = (EditText)dialogView.findViewById(R.id.rightInterval);

            typeSelectors[0] = (Spinner)dialogView.findViewById(R.id.type1Selector);
            typeSelectors[1] = (Spinner)dialogView.findViewById(R.id.type2Selector);
        }

        public String getInputName() {
            return nameText.getText().toString();
        }

        public boolean constrainedByLeftInterval() {
            return leftIntervalBox.isChecked();
        }

        public boolean constrainedByRightInterval() {
            return rightIntervalBox.isChecked();
        }

        public float getLeftIntervalVal() {
            return Float.valueOf(leftInterval.getText().toString());
        }

        public float getRightIntervalVal() {
            return Float.valueOf(rightInterval.getText().toString());
        }

        public boolean conditionIsUsed(int index) {
            if(index < 3) {
                return conditionBoxes[index].isChecked();
            }
            else {
                return false;
            }
        }

        public int getSelectedType(int typeIndex) {
            int selectedPos = typeSelectors[typeIndex].getSelectedItemPosition();
            if(selectedPos == 0)
                return -1; //none is not part of original data
            else
                return selectedPos - 1;
        }

        public void setTypeList(int typeIndex, ArrayList<String> typeList) {
            if(!typeList.contains("none"))
                typeList.add(0, "none");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(dialogView.getContext(),android.R.layout.simple_spinner_item,typeList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSelectors[typeIndex].setAdapter(adapter);

        }

    }

    //memory managing code
    @Override
    public void onDestroy() {
        releaseAll();
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
//        if(level == TRIM_MEMORY_UI_HIDDEN) { //release whenever UI is hidden
//            releaseViewRelatedResource();
//        }

        if(level == TRIM_MEMORY_MODERATE) { //we should start to release some resources
            releaseViewRelatedResource();
        }

        super.onTrimMemory(level);
    }

    //we should recover these in onCreateView
    private void releaseViewRelatedResource() {
        fragmentView = null;
        infoText = null;
        alertDialog = null;
        dialogViewHolder = null;
        adapter.releaseAll();
        adapter = null;
    }

    private void releaseAll() {
        releaseViewRelatedResource();
        searchResult = null;
        typeList = null;
    }

}
