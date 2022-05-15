package com.example.taskmaster;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.taskmaster.activity.HomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspresoTest {

    @Rule
    public ActivityScenarioRule<HomeActivity> activityScenarioRule = new ActivityScenarioRule<HomeActivity>(HomeActivity.class);


    @Test
    public void AddTaskTest() {

        onView(withId(R.id.buttonAddTask)).perform(click());
        onView(withId(R.id.textViewTaskActivity)).check(matches(withText("Add Task")));

        onView(withId(R.id.editTextTaskTitle)).perform(typeText("espresso test1"), closeSoftKeyboard());
        onView(withId(R.id.editTextTaskDescription)).perform(typeText("testing add task page"), closeSoftKeyboard());
        onView(withId(R.id.buttonAddTaskTaskActivity)).perform(click());

        Espresso.pressBack();
    }

    @Test
    public void RecyclerViewAfterAddingTask() {
        onView(withId(R.id.homeTaskRecycleView)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.textTaskViewTitle)).check(matches(withText("espresso test1")));
        onView(withId(R.id.textTaskViewBody)).check(matches(withText("testing add task page")));
    }
    @Test
    public void test_The_Main_Activity(){
        onView(withId(R.id.textView5)).check(matches(withText("My Tasks")));
    }
    @Test
    public void test_All_Tasks_Activity(){
        onView(withId(R.id.buttonAllTask)).perform(click());
        onView(withId(R.id.textViewAllTasksHeader)).check(matches(withText("All Tasks")));
    }
    @Test
    public void test_userSettingsImage_Activity(){
        onView(withId(R.id.userSettingsImage)).perform(click());
        onView(withId(R.id.textView3)).check(matches(withText("User Settings")));
    }
    @Test
    public void test_userSettings(){
        onView(withId(R.id.userSettingsImage)).perform(click());
        onView(withId(R.id.editTextUserSettingsUsername)).perform(replaceText("espresso"), closeSoftKeyboard());
        onView(withId(R.id.buttonUserSettingsSaveUsername)).perform(click());

        onView(withId(R.id.textHomeUsernameView)).check(matches(withText("Username: "+"espresso")));
    }













}
