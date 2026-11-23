
package com.darkmatter.nodus.ui.shell

import nodus.shared.generated.resources.Res
import nodus.shared.generated.resources.ic_focus
import nodus.shared.generated.resources.ic_goal_lab
import nodus.shared.generated.resources.ic_profile
import nodus.shared.generated.resources.ic_star_map
import org.jetbrains.compose.resources.DrawableResource

enum class ShellTab(val title: String, val icon: DrawableResource) {
    GoalLab("Goal Lab", Res.drawable.ic_goal_lab),
    StarMap("Star Map", Res.drawable.ic_star_map),
    Focus("Focus", Res.drawable.ic_focus),
    Profile("Profile", Res.drawable.ic_profile),
}
