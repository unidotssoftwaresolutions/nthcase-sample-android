package com.nthcase.sample.lobby

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.AttrRes
import androidx.compose.ui.graphics.toArgb
import androidx.core.widget.NestedScrollView
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.materialswitch.MaterialSwitch
import com.nthcase.sample.R
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.Item
import com.nthcase.sample.data.Lobby
import com.nthcase.sample.data.Touch
import com.nthcase.sample.data.TouchLog
import com.nthcase.sample.ui.Art
import com.nthcase.sample.ui.ArtPaths
import com.nthcase.sample.ui.theme.DarkAisles
import com.nthcase.sample.ui.theme.LightAisles
import kotlin.math.ceil
import kotlin.math.roundToInt
import com.google.android.material.R as M

/**
 * The Lobby built with Android Views (SPEC.md §5 and §12, spec 1.1), the "Lobby (Views)" row of the Lab. The page is a
 * NestedScrollView; the pager a ViewPager2 above it; the chips and rows horizontal RecyclerViews; the grid a platform
 * GridLayout; the inner list a RecyclerView nested in the page; the board a BoardView, which scrolls both ways; the bar
 * Material's BottomNavigationView; the quick sheet Material's BottomSheetDialog. The Top 10 row is a horizontal
 * RecyclerView between the pager and the page, outside the page; "Show the pager" off removes the pager's frame from
 * its parent, so the row is then the first scrollable. Every id is a view id from res/values/lobby_ids.xml, so Appium
 * reads `com.nthcase.sample:id/lobby_pager`.
 *
 * Touches are the views' own click and long click. A tap is counted once the double-tap time has passed without a
 * second click, so a double tap is one line in the log. Nothing here adds a rule of its own to a moving finger.
 */
class LobbyViewsFragment : Fragment(R.layout.fragment_lobby) {
    val log = TouchLog()
    private val handler = Handler(Looper.getMainLooper())
    private val pendingTaps = HashMap<View, Runnable>()
    private val ids = HashMap<String, Int>()
    private var chipSelected = 1
    private var barChecked = 0
    private var savedShown = false
    private var sheet: BottomSheetDialog? = null

    private lateinit var top: LinearLayout
    private lateinit var framePager: View
    private lateinit var page: NestedScrollView
    private lateinit var pager: ViewPager2
    private lateinit var auto: MaterialSwitch
    private lateinit var bar: BottomNavigationView
    private lateinit var detail: LinearLayout
    private lateinit var promo: LinearLayout
    private lateinit var saved: LinearLayout
    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = closePanel()
    }
    private val showSaved = Runnable { setSaved(true) }
    private val advance = object : Runnable {
        override fun run() {
            pager.setCurrentItem((pager.currentItem + 1) % Lobby.BANNERS, true)
            handler.postDelayed(this, Lobby.AUTO_ADVANCE_MS)
        }
    }

    /** A spec id's view id, from res/values/lobby_ids.xml. */
    @SuppressLint("DiscouragedApi")
    fun idOf(name: String): Int = ids.getOrPut(name) {
        resources.getIdentifier(name, "id", requireContext().packageName).also { check(it != 0) { "$name is not in lobby_ids.xml" } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        top = view.findViewById(R.id.frame_top)
        framePager = view.findViewById(R.id.frame_pager)
        page = view.findViewById(R.id.lobby_page)
        pager = view.findViewById(R.id.lobby_pager)
        bar = view.findViewById(R.id.lobby_bar)

        val logView = view.findViewById<TextView>(R.id.lobby_touch_log)
        val countView = view.findViewById<TextView>(R.id.lobby_touch_count)
        log.onChange = {
            logView.text = log.text
            countView.text = log.countText
        }
        log.onChange?.invoke()
        view.findViewById<View>(R.id.lobby_touch_reset).setOnClickListener { log.reset() }

        buildDelayChoice(view.findViewById(R.id.lobby_saved_delay))
        buildPager(view.findViewById(R.id.lobby_pager_position), view.findViewById(R.id.lobby_pager_auto))
        buildTopRow()
        buildPagerShown(view.findViewById(R.id.lobby_pager_shown))
        buildPage(view.findViewById(R.id.frame_page_content))
        buildPanels(view.findViewById(R.id.frame_middle))
        buildBar()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        startSavedClock()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        pendingTaps.clear()
        log.onChange = null
        sheet?.dismiss()
        sheet = null
        super.onDestroyView()
    }

    // ── touches ──────────────────────────────────────────────────────────────────────────────────────────────────────

    private fun touchable(v: View, what: String, where: String, onTap: (() -> Unit)? = null, onLongPress: (() -> Unit)? = null) {
        v.setOnClickListener { clicked ->
            val pending = pendingTaps.remove(clicked)
            if (pending != null) {
                handler.removeCallbacks(pending)
                log.add(Touch.DoubleTap, what, where)
            } else {
                val tap = Runnable {
                    pendingTaps.remove(clicked)
                    log.add(Touch.Tap, what, where)
                    onTap?.invoke()
                }
                pendingTaps[clicked] = tap
                handler.postDelayed(tap, ViewConfiguration.getDoubleTapTimeout().toLong())
            }
        }
        v.setOnLongClickListener {
            log.add(Touch.LongPress, what, where)
            onLongPress?.invoke()
            true
        }
    }

    // ── saved appears late ───────────────────────────────────────────────────────────────────────────────────────────

    /** The count starts when the Lobby opens and again when a delay is chosen. Until it ends, Saved is not in the bar. */
    private fun startSavedClock() {
        handler.removeCallbacks(showSaved)
        val seconds = AppState.lobbySavedDelay
        setSaved(seconds == 0)
        if (seconds > 0) handler.postDelayed(showSaved, seconds * 1000L)
    }

    private fun setSaved(shown: Boolean) {
        savedShown = shown
        val id = idOf("lobby_bar_saved")
        val menu = bar.menu
        if (shown && menu.findItem(id) == null) {
            menu.add(Menu.NONE, id, 2, Lobby.barItems[2]).setIcon(barIcon(2))
        } else if (!shown && menu.findItem(id) != null) {
            // removed, not hidden: the bar has three items and the tree has no node for Saved
            menu.removeItem(id)
            if (barChecked == 2) barChecked = 0
        }
        menu.findItem(barItemId(barChecked))?.isChecked = true
    }

    private fun buildDelayChoice(group: MaterialButtonToggleGroup) {
        Lobby.DELAYS.forEach { seconds ->
            val button = MaterialButton(requireContext(), null, M.attr.materialButtonOutlinedStyle).apply {
                id = idOf("lobby_saved_delay_$seconds")
                text = "$seconds s"
                minWidth = 0
                minimumWidth = 0
                setPadding(0, paddingTop, 0, paddingBottom)
                setOnClickListener {
                    AppState.lobbySavedDelay = seconds
                    markDelay(group)
                    startSavedClock()
                }
            }
            group.addView(button, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        markDelay(group)
    }

    /** The chosen delay is checked (filled) and bold, so it is not told by colour alone. */
    private fun markDelay(group: MaterialButtonToggleGroup) {
        val chosen = idOf("lobby_saved_delay_${AppState.lobbySavedDelay}")
        group.check(chosen)
        for (i in 0 until group.childCount) {
            val b = group.getChildAt(i) as MaterialButton
            b.setTypeface(null, if (b.id == chosen) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    // ── the pager ────────────────────────────────────────────────────────────────────────────────────────────────────

    private fun buildPager(position: LinearLayout, auto: MaterialSwitch) {
        this.auto = auto
        pager.adapter = BannerAdapter()
        val dots = List(Lobby.BANNERS) { View(requireContext()).also { position.addView(it) } }
        val label = TextView(requireContext()).apply { textAppearance(M.attr.textAppearanceLabelMedium) }
        position.addView(label, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginStart = 6.dp })
        fun show(page: Int) {
            val text = Lobby.pagerPosition(page + 1)
            label.text = text
            position.contentDescription = text
            dots.forEachIndexed { i, dot ->
                val on = i == page
                val size = (if (on) 10 else 6).dp
                dot.layoutParams = LinearLayout.LayoutParams(size, size).apply { marginEnd = 6.dp }
                dot.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color(if (on) androidx.appcompat.R.attr.colorPrimary else M.attr.colorOutlineVariant))
                }
            }
        }
        show(0)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = show(position)
        })
        auto.setOnCheckedChangeListener { _, on ->
            handler.removeCallbacks(advance)
            if (on) handler.postDelayed(advance, Lobby.AUTO_ADVANCE_MS)
        }
    }

    /** "Show the pager": off removes the pager's frame from the tree (removeView, not GONE); on puts it back, first. */
    private fun buildPagerShown(switch: MaterialSwitch) {
        switch.isChecked = AppState.lobbyPagerShown
        showPager(AppState.lobbyPagerShown)
        switch.setOnCheckedChangeListener { _, on ->
            AppState.lobbyPagerShown = on
            showPager(on)
        }
    }

    private fun showPager(shown: Boolean) {
        val attached = framePager.parent != null
        if (shown && !attached) {
            top.addView(framePager, 0)
            handler.removeCallbacks(advance)
            if (auto.isChecked) handler.postDelayed(advance, Lobby.AUTO_ADVANCE_MS)
        } else if (!shown && attached) {
            handler.removeCallbacks(advance)
            top.removeView(framePager)
        }
    }

    // ── the Top 10 row ───────────────────────────────────────────────────────────────────────────────────────────────

    /** Fixed, outside the page, under the pager or in its place: nothing vertical encloses it. */
    private fun buildTopRow() {
        val position = heading(Lobby.topRowPosition(1, 3)).apply {
            id = idOf("lobby_top_row_position")
            contentDescription = text
            setPadding(16.dp, 8.dp, 16.dp, 4.dp)
        }
        top.addView(position)
        val list = horizontalList().apply {
            id = idOf("lobby_top_row")
            setPadding(16.dp, 0, 16.dp, 4.dp)
            adapter = TopTileAdapter()
        }
        top.addView(list)
        val lm = list.layoutManager as LinearLayoutManager
        fun update() {
            var first = lm.findFirstCompletelyVisibleItemPosition()
            var last = lm.findLastCompletelyVisibleItemPosition()
            if (first < 0) { first = lm.findFirstVisibleItemPosition().coerceAtLeast(0); last = first }
            val text = Lobby.topRowPosition(first + 1, last + 1)
            position.text = text
            position.contentDescription = text
        }
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = update()
        })
        list.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> update() }
    }

    private inner class TopTileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount() = Lobby.TOP_TILES
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val tile = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RecyclerView.LayoutParams(100.dp, ViewGroup.LayoutParams.WRAP_CONTENT)
                foreground = selectable()
            }
            // a band 100 × 64, lower than a page row's tile, so the page keeps its room under the fixed row
            tile.addView(ImageView(parent.context), LinearLayout.LayoutParams(100.dp, 64.dp))
            tile.addView(text(M.attr.textAppearanceBodySmall).apply {
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(0, 4.dp, 0, 0)
            })
            return object : RecyclerView.ViewHolder(tile) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val m = position + 1
            val item = Lobby.topItem(m)
            val tile = holder.itemView as LinearLayout
            tile.id = idOf("lobby_top_row_tile_$m")
            tile.contentDescription = item.name
            (tile.getChildAt(0) as ImageView).setImageDrawable(glyph(item, 8.dp.toFloat()))
            (tile.getChildAt(1) as TextView).text = item.name
            touchable(
                tile, item.name, Lobby.tileWhere(Lobby.TOP_ROW, m),
                onTap = { showDetail(Lobby.TOP_ROW, m) },
                onLongPress = { openSheet(item.name) { showDetail(Lobby.TOP_ROW, m) } },
            )
        }
    }

    private inner class BannerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount() = Lobby.BANNERS
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val frame = FrameLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setPadding(16.dp, 0, 16.dp, 0)
            }
            val card = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16.dp, 16.dp, 16.dp, 16.dp)
                background = rounded(color(M.attr.colorPrimaryContainer), 12.dp.toFloat())
                foreground = selectable()
            }
            card.addView(text(M.attr.textAppearanceTitleMedium, color(M.attr.colorOnPrimaryContainer)))
            card.addView(text(M.attr.textAppearanceBodySmall, color(M.attr.colorOnPrimaryContainer)).apply { this.text = "A test banner" })
            frame.addView(card, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            return object : RecyclerView.ViewHolder(frame) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val n = position + 1
            val title = Lobby.banners[position]
            val card = (holder.itemView as FrameLayout).getChildAt(0) as LinearLayout
            card.id = idOf("lobby_banner_$n")
            (card.getChildAt(0) as TextView).text = title
            touchable(card, title, "banner $n", onTap = { showPromo(n) }, onLongPress = { openSheet(title) { showPromo(n) } })
        }
    }

    // ── the page ─────────────────────────────────────────────────────────────────────────────────────────────────────

    private fun buildPage(content: LinearLayout) {
        content.addView(chips())
        for (row in 1..Lobby.ROWS) tileRow(content, row)
        content.addView(heading("Grid"))
        content.addView(grid())
        content.addView(heading("Inner list"))
        content.addView(innerList())
        board(content)
        content.addView(footer())
    }

    private fun horizontalList(): RecyclerView = RecyclerView(requireContext()).apply {
        layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        clipToPadding = false
        setPadding(16.dp, 0, 16.dp, 0)
        addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.getChildAdapterPosition(view) > 0) outRect.left = 8.dp
            }
        })
    }

    private fun chips(): RecyclerView = horizontalList().apply {
        id = idOf("lobby_chips")
        setPadding(16.dp, 8.dp, 16.dp, 8.dp)
        adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount() = Lobby.chips.size
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = object : RecyclerView.ViewHolder(
                Chip(parent.context).apply { isCheckable = false },
            ) {}

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val n = position + 1
                val label = Lobby.chips[position]
                val on = n == chipSelected
                (holder.itemView as Chip).apply {
                    id = idOf("lobby_chip_$n")
                    text = label
                    isSelected = on
                    chipIcon = if (on) GlyphDrawable(ArtPaths.CHECK, 24f, 2f, color(M.attr.colorOnSecondaryContainer)) else null
                    isChipIconVisible = on
                    chipBackgroundColor = ColorStateList.valueOf(color(if (on) M.attr.colorSecondaryContainer else M.attr.colorSurface))
                    chipStrokeColor = ColorStateList.valueOf(color(if (on) M.attr.colorSecondaryContainer else M.attr.colorOutline))
                    touchable(this, label, "chip $n", onTap = {
                        val before = chipSelected
                        chipSelected = n
                        notifyItemChanged(before - 1)
                        notifyItemChanged(n - 1)
                    })
                }
            }
        }
    }

    private fun tileRow(content: LinearLayout, row: Int) {
        val position = heading("").apply { id = idOf("lobby_row_${row}_position") }
        content.addView(position)
        val list = horizontalList().apply {
            id = idOf("lobby_row_$row")
            adapter = TileAdapter(row)
        }
        content.addView(list)
        val lm = list.layoutManager as LinearLayoutManager
        fun update() {
            var first = lm.findFirstCompletelyVisibleItemPosition()
            var last = lm.findLastCompletelyVisibleItemPosition()
            if (first < 0) { first = lm.findFirstVisibleItemPosition().coerceAtLeast(0); last = first }
            val text = Lobby.rowPosition(row, first + 1, last + 1)
            position.text = text
            position.contentDescription = text
        }
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = update()
        })
        list.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> update() }
        position.text = Lobby.rowPosition(row, 1, 3)
    }

    private inner class TileAdapter(val row: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount() = Lobby.TILES
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val tile = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = RecyclerView.LayoutParams(100.dp, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(0, 0, 0, 4.dp)
                foreground = selectable()
            }
            tile.addView(ImageView(parent.context), LinearLayout.LayoutParams(100.dp, 100.dp))
            tile.addView(text(M.attr.textAppearanceBodySmall).apply {
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(0, 4.dp, 0, 0)
            })
            tile.addView(itemNumber())
            return object : RecyclerView.ViewHolder(tile) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val m = position + 1
            val item = Lobby.tileItem(row, m)
            val tile = holder.itemView as LinearLayout
            tile.id = idOf("lobby_row_${row}_tile_$m")
            tile.contentDescription = "${item.name}, ${item.number}"
            (tile.getChildAt(0) as ImageView).setImageDrawable(glyph(item, 8.dp.toFloat()))
            (tile.getChildAt(1) as TextView).text = item.name
            (tile.getChildAt(2) as TextView).text = item.number
            touchable(
                tile, item.name, "row $row, tile $m",
                onTap = { showDetail(row, m) },
                onLongPress = { openSheet(item.name) { showDetail(row, m) } },
            )
        }
    }

    private fun grid(): GridLayout = GridLayout(requireContext()).apply {
        id = idOf("lobby_grid")
        columnCount = 2
        setPadding(12.dp, 0, 12.dp, 0)
        for (n in 1..Lobby.GRID_CELLS) {
            val item = Lobby.gridItem(n)
            val cell = LinearLayout(context).apply {
                id = idOf("lobby_grid_cell_$n")
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(8.dp, 8.dp, 8.dp, 8.dp)
                background = rounded(0, 8.dp.toFloat(), stroke = color(M.attr.colorOutlineVariant))
                foreground = selectable()
            }
            cell.addView(ImageView(context).apply { setImageDrawable(glyph(item, 8.dp.toFloat())) }, LinearLayout.LayoutParams(40.dp, 40.dp))
            cell.addView(text(M.attr.textAppearanceBodySmall).apply {
                text = item.name
                maxLines = 2
                setPadding(8.dp, 0, 0, 0)
            })
            touchable(cell, item.name, "grid, cell $n")
            val r = (n - 1) / 2
            val c = (n - 1) % 2
            addView(cell, GridLayout.LayoutParams(GridLayout.spec(r), GridLayout.spec(c, 1f)).apply {
                width = 0
                setMargins(4.dp, 4.dp, 4.dp, 4.dp)
            })
        }
    }

    private fun innerList(): RecyclerView = RecyclerView(requireContext()).apply {
        id = idOf("lobby_inner_list")
        layoutManager = LinearLayoutManager(requireContext())
        isNestedScrollingEnabled = true
        background = rounded(0, 8.dp.toFloat(), stroke = color(M.attr.colorOutlineVariant))
        addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 240.dp).apply { setMargins(16.dp, 0, 16.dp, 0) }
        adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount() = Lobby.INNER_LINES
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = object : RecyclerView.ViewHolder(
                text(M.attr.textAppearanceBodyMedium).apply {
                    layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    setPadding(12.dp, 14.dp, 12.dp, 14.dp)
                    foreground = selectable()
                },
            ) {}

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val n = position + 1
                (holder.itemView as TextView).apply {
                    id = idOf("lobby_inner_row_$n")
                    text = Lobby.innerLine(n)
                    touchable(this, "Line $n", "inner list")
                }
            }
        }
    }

    private fun board(content: LinearLayout) {
        val cell = 56.dp
        val position = heading(Lobby.boardPosition(1, 1)).apply {
            id = idOf("lobby_board_position")
            contentDescription = text
        }
        content.addView(position)
        val board = BoardView(requireContext()).apply {
            id = idOf("lobby_board")
            background = rounded(0, 4.dp.toFloat(), stroke = color(M.attr.colorOutline))
        }
        val cells = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(color(M.attr.colorOutlineVariant))
        }
        for (row in 1..Lobby.BOARD) {
            val line = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
            for (col in 1..Lobby.BOARD) {
                val label = Lobby.boardLabel(col, row)
                val v = text(M.attr.textAppearanceLabelMedium).apply {
                    id = idOf("lobby_board_cell_${'a' + (col - 1)}$row")
                    text = label
                    gravity = Gravity.CENTER
                    setBackgroundColor(color(M.attr.colorSurface))
                    foreground = selectable()
                }
                touchable(v, label, "board")
                // a one-pixel gap on two sides draws the grid lines in the board's outline colour
                line.addView(v, LinearLayout.LayoutParams(cell - 1, cell - 1).apply { setMargins(0, 0, 1, 1) })
            }
            cells.addView(line)
        }
        board.addView(cells)
        board.onScrolled = { x, y ->
            val col = ceil(x / cell.toFloat() - 0.001f).toInt() + 1
            val row = ceil(y / cell.toFloat() - 0.001f).toInt() + 1
            val text = Lobby.boardPosition(col.coerceIn(1, Lobby.BOARD), row.coerceIn(1, Lobby.BOARD))
            position.text = text
            position.contentDescription = text
        }
        val frame = FrameLayout(requireContext())
        frame.addView(board, FrameLayout.LayoutParams(cell * 5, cell * 5, Gravity.CENTER_HORIZONTAL))
        content.addView(frame)
    }

    private fun footer(): View = LinearLayout(requireContext()).apply {
        id = idOf("lobby_footer")
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(0, 24.dp, 0, 48.dp)
        addView(text(M.attr.textAppearanceTitleMedium).apply { text = Lobby.FOOTER })
        addView(text(M.attr.textAppearanceBodySmall, color(M.attr.colorOnSurfaceVariant)).apply { text = Lobby.FOOTER_NOTE })
    }

    // ── panels, the sheet and the bar ────────────────────────────────────────────────────────────────────────────────

    private fun panel(id: String): LinearLayout = LinearLayout(requireContext()).apply {
        this.id = idOf(id)
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(24.dp, 24.dp, 24.dp, 24.dp)
        visibility = View.GONE
    }

    private fun backButton(id: String) = MaterialButton(requireContext(), null, M.attr.materialButtonOutlinedStyle).apply {
        this.id = idOf(id)
        text = Lobby.BACK_TO_LOBBY
        setOnClickListener { closePanel() }
    }

    private fun buildPanels(middle: FrameLayout) {
        detail = panel("lobby_detail").apply {
            addView(ImageView(context), LinearLayout.LayoutParams(96.dp, 96.dp))
            addView(text(M.attr.textAppearanceHeadlineSmall).apply { gravity = Gravity.CENTER; setPadding(0, 8.dp, 0, 0) })
            addView(itemNumber())
            addView(text(M.attr.textAppearanceBodyMedium).apply { setPadding(0, 8.dp, 0, 8.dp) })
            addView(backButton("lobby_detail_back"))
        }
        promo = panel("lobby_promo").apply {
            addView(text(M.attr.textAppearanceHeadlineSmall).apply { gravity = Gravity.CENTER })
            addView(text(M.attr.textAppearanceLabelLarge).apply { setPadding(0, 8.dp, 0, 8.dp) })
            addView(text(M.attr.textAppearanceBodyMedium).apply { text = Lobby.PROMO_NOTE; gravity = Gravity.CENTER; setPadding(0, 0, 0, 8.dp) })
            addView(backButton("lobby_promo_back"))
        }
        saved = panel("lobby_saved_screen").apply {
            addView(ImageView(context).apply {
                setImageDrawable(GlyphDrawable(ArtPaths.CHECK, 24f, 2f, color(M.attr.colorOnSurfaceVariant)))
            }, LinearLayout.LayoutParams(40.dp, 40.dp))
            addView(text(M.attr.textAppearanceTitleLarge).apply { text = Lobby.SAVED_EMPTY; setPadding(0, 8.dp, 0, 0) })
            contentDescription = Lobby.SAVED_EMPTY
        }
        listOf(detail, promo, saved).forEach { middle.addView(it, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)) }
    }

    /** A panel takes the place of the pager, the Top 10 row and the page; they keep their scroll positions meanwhile. */
    private fun showPanel(shown: View) {
        top.visibility = View.GONE
        page.visibility = View.GONE
        listOf(detail, promo, saved).forEach { it.visibility = if (it === shown) View.VISIBLE else View.GONE }
        backCallback.isEnabled = shown !== saved
    }

    private fun closePanel() {
        listOf(detail, promo, saved).forEach { it.visibility = View.GONE }
        top.visibility = View.VISIBLE
        page.visibility = View.VISIBLE
        backCallback.isEnabled = false
    }

    /** [row] 1–4, or [Lobby.TOP_ROW] for the Top 10 row. */
    private fun showDetail(row: Int, tile: Int) {
        val item = Lobby.itemAt(row, tile)
        (detail.getChildAt(0) as ImageView).apply {
            setImageDrawable(glyph(item, 8.dp.toFloat()))
            contentDescription = "Drawing of the ${item.name}"
        }
        (detail.getChildAt(1) as TextView).text = item.name
        (detail.getChildAt(2) as TextView).text = item.number
        (detail.getChildAt(3) as TextView).text = Lobby.detailPlace(row, tile)
        detail.contentDescription = "${item.name}, ${item.number}, ${Lobby.detailPlace(row, tile)}"
        showPanel(detail)
    }

    private fun showPromo(n: Int) {
        (promo.getChildAt(0) as TextView).text = Lobby.banners[n - 1]
        (promo.getChildAt(1) as TextView).text = Lobby.pagerPosition(n)
        promo.contentDescription = "${Lobby.banners[n - 1]}, ${Lobby.pagerPosition(n)}"
        showPanel(promo)
    }

    private fun openSheet(title: String, open: () -> Unit) {
        sheet?.dismiss()
        val dialog = BottomSheetDialog(requireContext())
        val content = LinearLayout(requireContext()).apply {
            id = idOf("lobby_sheet")
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp, 16.dp, 16.dp, 24.dp)
            addView(text(M.attr.textAppearanceTitleLarge).apply {
                id = idOf("lobby_sheet_title")
                text = title
                isAccessibilityHeading = true
                setPadding(0, 0, 0, 12.dp)
            })
        }
        listOf("lobby_sheet_open", "lobby_sheet_preview", "lobby_sheet_saved", "lobby_sheet_rate").forEachIndexed { i, id ->
            val label = Lobby.sheetButtons[i]
            content.addView(MaterialButton(requireContext(), null, androidx.appcompat.R.attr.borderlessButtonStyle).apply {
                this.id = idOf(id)
                text = label
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                minHeight = 48.dp
                setOnClickListener {
                    log.add(Touch.Tap, label, "quick sheet, $title")
                    dialog.dismiss()
                    if (label == "Open") open()
                }
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        dialog.setContentView(content)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        dialog.setOnDismissListener { if (sheet === dialog) sheet = null }
        sheet = dialog
        dialog.show()
    }

    /** The quick sheet that is open, if any: the tests read its window. */
    val openSheet: BottomSheetDialog? get() = sheet

    private fun barItemId(i: Int) = idOf(listOf("lobby_bar_home", "lobby_bar_live", "lobby_bar_saved", "lobby_bar_offers")[i])

    private fun barIcon(i: Int) = GlyphDrawable(listOf(ArtPaths.SHOP, ArtPaths.EYE, ArtPaths.CHECK, ArtPaths.CART)[i], 24f, 2f, color(M.attr.colorOnSurface))

    private fun buildBar() {
        val menu = bar.menu
        Lobby.barItems.indices.forEach { i -> if (i != 2) menu.add(Menu.NONE, barItemId(i), i, Lobby.barItems[i]).setIcon(barIcon(i)) }
        val onItem = { item: android.view.MenuItem ->
            val i = (0..3).first { barItemId(it) == item.itemId }
            log.add(Touch.Tap, Lobby.barItems[i], "bar")
            barChecked = i
            if (i == 2) showPanel(saved) else closePanel()
        }
        bar.setOnItemSelectedListener { onItem(it); true }
        bar.setOnItemReselectedListener { onItem(it) }
    }

    // ── small helpers ────────────────────────────────────────────────────────────────────────────────────────────────

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).roundToInt()

    private fun color(@AttrRes attr: Int): Int = MaterialColors.getColor(requireView(), attr)

    private fun TextView.textAppearance(@AttrRes attr: Int) {
        val value = TypedValue()
        context.theme.resolveAttribute(attr, value, true)
        TextViewCompat.setTextAppearance(this, value.resourceId)
    }

    private fun text(@AttrRes appearance: Int, textColor: Int? = null): TextView = TextView(requireContext()).apply {
        textAppearance(appearance)
        textColor?.let { setTextColor(it) }
    }

    private fun heading(text: String): TextView = text(M.attr.textAppearanceTitleSmall, color(androidx.appcompat.R.attr.colorPrimary)).apply {
        this.text = text
        isAccessibilityHeading = true
        setPadding(16.dp, 16.dp, 16.dp, 8.dp)
    }

    /** "No. 007" in brass monospace, as on every screen. */
    private fun itemNumber(): TextView = TextView(requireContext()).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        letterSpacing = 0.04f
        setTextColor(color(M.attr.colorTertiary))
    }

    private fun rounded(fill: Int, radius: Float, stroke: Int? = null) = GradientDrawable().apply {
        cornerRadius = radius
        setColor(fill)
        stroke?.let { setStroke(1.dp, it) }
    }

    private fun selectable() = TypedValue().let { value ->
        requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)
        requireContext().getDrawable(value.resourceId)
    }

    private fun glyph(item: Item, corner: Float): GlyphDrawable {
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val tint = (if (dark) DarkAisles else LightAisles).of(item.category)
        return GlyphDrawable(Art.pathOf(item.glyph), 48f, 2.5f, tint.ink.toArgb(), tint.tile.toArgb(), corner, scale = 0.62f)
    }
}
