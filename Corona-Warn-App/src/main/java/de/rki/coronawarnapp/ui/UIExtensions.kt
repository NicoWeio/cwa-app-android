package de.rki.coronawarnapp.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragment
import de.rki.coronawarnapp.databinding.ActivityMainBinding
import de.rki.coronawarnapp.ui.main.home.HomeFragment
import java.lang.ref.WeakReference

/**
 * Extends NavController to prevent navigation error when the user clicks on two buttons at almost
 * the exact time.
 *
 * @see [NavController]
 */
fun NavController.doNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)
        ?.let { navigate(direction) }
}

/**
 * Similar to [setupWithNavController],but it executes the passed action on item selection
 * and shows [BottomNavigationView] on [HomeFragment], [ContactDiaryOverviewFragment] only
 */
fun ActivityMainBinding.setupWithNavController2(
    navController: NavController,
    onItemSelected: () -> Unit,
    onDestinationChanged: (Boolean) -> Unit,
) {
    mainBottomNavigation.setupWithNavController(navController)
    mainBottomNavigation.setOnItemSelectedListener { item ->
        onItemSelected()
        NavigationUI.onNavDestinationSelected(item, navController)
    }
    val weakBottomNavView = WeakReference(this)
    navController.addOnDestinationChangedListener(
        object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                val bottomView = weakBottomNavView.get()
                // Remove listener if View does not exit
                if (bottomView == null) {
                    navController.removeOnDestinationChangedListener(this)
                    return
                }
                // For destinations that always show the bottom bar
                val inShowList = destination.id in listOf(
                    R.id.mainFragment,
                    R.id.checkInsFragment,
                    R.id.contactDiaryOverviewFragment,
                    R.id.personOverviewFragment,
                )
                // For destinations that can show or hide the bottom bar in different cases
                // for example [ContactDiaryOnboardingFragment]
                val hasShowArgument = arguments?.getBoolean("showBottomNav") ?: false

                val isVisible = inShowList || hasShowArgument
                if (isVisible) showBottomBar() else hideBottomAppBar()
                onDestinationChanged(isVisible)
            }
        }
    )
}

private fun ActivityMainBinding.showBottomBar() {
    bottomAppBar.isVisible = true
    bottomAppBar.performShow()
    scannerFab.show()
}

private fun ActivityMainBinding.hideBottomAppBar() {
    bottomAppBar.performHide()
    bottomAppBar.animate().setListener(object : AnimatorListenerAdapter() {
        var isCanceled = false
        override fun onAnimationEnd(animation: Animator?) {
            if (isCanceled) return
            bottomAppBar.visibility = View.GONE
            scannerFab.visibility = View.INVISIBLE
        }

        override fun onAnimationCancel(animation: Animator?) {
            isCanceled = true
        }
    })
}
