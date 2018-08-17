package jp.kuluna.eventgridview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import java.util.*

/**
 * CounterGridView用Adapter
 * @param context [Context]
 */
open class CounterGridAdapter(private val context: Context) : RecyclerView.Adapter<CounterGridViewHolder>() {
    /** Counterのクリックイベント */
    var onCounterClickListener: ((Counter) -> Unit)? = null

    private var counters = emptyList<Counter>()
    /** 最後の終了時刻 */
    private val lastEnd
        get() = counters.maxBy { it.end }?.end
    /** 基準日 */
    var day = Date()
    /** 24時間を超えた時間 */
    val overTime: Int
        get() {
            val selectCal = Calendar.getInstance()
            selectCal.time = day
            val lastEndCal = Calendar.getInstance()
            lastEndCal.time = lastEnd ?: day

            return if (selectCal.get(Calendar.DATE) != lastEndCal.get(Calendar.DATE)) {//日跨ぎ有り
                lastEndCal.get(Calendar.HOUR_OF_DAY)
            } else {//日跨ぎ無し
                -1
            }
        }
    /** CounterViewColumnで生成されたのCounterView格納用 */
    private var counterViews = mutableListOf<View>()
    /** ViewHolder全体のCounterViewの配列の格納用 */
    private var counterViewGroup = mutableListOf<List<View>>()

    private var scaleFrom: Int = 0
    private var scaleTo: Int = 23

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterGridViewHolder = CounterGridViewHolder(CounterColumnView(context, scaleFrom))

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: CounterGridViewHolder, position: Int) {
        val counter = counters
        holder.view.set(day, counter, holder.layoutPosition)
        holder.view.onCounterClickListener = {
            onCounterClickListener?.invoke(it)
        }
        counterViews = holder.view.counterViews
        counterViewGroup.add(counterViews)
    }

    /**
     * カウンタを全てクリアして引数で渡すカウンタに差し替えます。
     * @param counters カウンタリスト
     */
    fun replace(counters: List<Counter>, day: Date) {
        this.counters = counters
        this.day = day

        notifyDataSetChanged()
    }

    /** 目盛りの範囲を設定します */
    internal fun setScale(from: Int, to: Int) {
        scaleFrom = from
        scaleTo = to
        replace(counters, day)
    }
}
