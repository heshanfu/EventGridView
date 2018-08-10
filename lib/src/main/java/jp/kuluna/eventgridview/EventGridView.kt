package jp.kuluna.eventgridview

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import jp.kuluna.eventgridview.databinding.ViewEventGridBinding
import java.util.*

class EventGridView : FrameLayout {
    private val binding: ViewEventGridBinding
    private var counterGridAdapter: CounterGridAdapter? = null
    private var limits: List<Limit> = emptyList()
    var adapter: EventGridAdapter?
        get() = binding.eventGridRecyclerView.adapter as? EventGridAdapter
        set(value) {
            binding.eventGridRecyclerView.adapter = value
            value?.onScaleRefreshListener = {
                binding.overTime = kotlin.math.max(it, counterGridAdapter?.overTime ?: -1)
                if (binding.counterVisibility) {
                    refreshCounter(value?.getEvents() ?: emptyList())
                }
            }
        }

    /**
     * Eventのカウンタを表示します
     * @param events 集計するEventのリスト
     * @param date 基準となる日付
     * @param limits 各時間帯の上限・下限値(任意)
     */
    fun showCounter(events: List<Event>, date: Date, limits: List<Limit> = emptyList()) {
        val counterGridAdapter = CounterGridAdapter(context)
        this.counterGridAdapter = counterGridAdapter
        binding.counterVisibility = true
        binding.counterGridRecyclerView.adapter = counterGridAdapter
        this.limits = limits

        // カウンタの状態が変わる区切りを保存する
        var periods = mutableListOf<Date>()
        for (event in events) {
            periods.add(event.start)
            periods.add(event.end)
        }
        for (limit in limits) {
            periods.add(limit.start)
            periods.add(limit.end)
        }
        periods = periods.distinct().sorted().toMutableList()

        // カウンタのリストを作成する
        val counters = mutableListOf<Counter>()
        for (i in 0..(periods.size - 2)) {
            val period = periods[i]
            val limit = limits.firstOrNull { it.start <= period && it.end > period }
            counters.add(Counter(periods[i], periods[i + 1], events.count { it.start <= period && it.end > period }, limit?.minimum, limit?.maximum))
        }
        counterGridAdapter.replace(counters, date)
        // 超過時間を再設定します
        binding.overTime = kotlin.math.max(adapter?.overTime ?: -1, counterGridAdapter.overTime)
    }

    /** カウンタを更新します */
    private fun refreshCounter(events: List<Event>) {
        val counterGridAdapter = this.counterGridAdapter ?: return
        // カウンタの状態が変わる区切りを保存する
        var periods = mutableListOf<Date>()
        for (event in events) {
            periods.add(event.start)
            periods.add(event.end)
        }
        for (limit in limits) {
            periods.add(limit.start)
            periods.add(limit.end)
        }
        periods = periods.distinct().sorted().toMutableList()

        // カウンタのリストを作成する
        val counters = mutableListOf<Counter>()
        for (i in 0..(periods.size - 2)) {
            val period = periods[i]
            val limit = limits.firstOrNull { it.start <= period && it.end > period }
            counters.add(Counter(periods[i], periods[i + 1], events.count { it.start <= period && it.end > period }, limit?.minimum, limit?.maximum))
        }
        counterGridAdapter.replace(counters, counterGridAdapter.day)
        // 超過時間を再設定します
        binding.overTime = kotlin.math.max(adapter?.overTime ?: -1, counterGridAdapter.overTime)
    }

    /** イベントにクリックリスナを実装します */
    fun setOnEventClickListener(onEventClickListener: ((Event) -> Unit)?) {
        adapter?.onEventClickListener = onEventClickListener
    }

    /** カウンタにクリックリスナを実装します */
    fun setOnCounterClickListener(onCounterClickListener: ((Counter) -> Unit)?) {
        counterGridAdapter?.onCounterClickListener = onCounterClickListener
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_event_grid, this, true)
        binding.eventGridRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.counterGridRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.eventGridRecyclerView.setOnTouchListener { _, event ->
            return@setOnTouchListener when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    adapter?.hideAllAdjustButton()
                    false
                }
                else -> false
            }
        }

        binding.counterVisibility = false
    }
}
