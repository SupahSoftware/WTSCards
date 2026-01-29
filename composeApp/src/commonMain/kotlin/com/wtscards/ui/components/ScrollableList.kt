package com.wtscards.ui.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wtscards.ui.theme.accentPrimary

/**
 * A reusable scrollable list component with a consistent scrollbar style. Wraps LazyColumn with
 * automatic scrollbar support.
 */
@Composable
fun ScrollableList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    scrollbarPadding: Dp = 24.dp,
    content: LazyListScope.() -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize().padding(end = scrollbarPadding),
            verticalArrangement = verticalArrangement,
            content = content
        )

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(state),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(end = 4.dp),
            style = defaultScrollbarStyle().copy(
                unhoverColor = accentPrimary.copy(alpha = 0.4f),
                hoverColor = accentPrimary.copy(alpha = 0.7f)
            )
        )
    }
}
