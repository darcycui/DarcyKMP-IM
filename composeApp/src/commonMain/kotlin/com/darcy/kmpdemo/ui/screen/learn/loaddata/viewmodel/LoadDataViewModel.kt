package com.darcy.kmpdemo.ui.screen.learn.loaddata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.darcy.kmpdemo.bean.http.response.LoadDataResponse
import com.darcy.kmpdemo.log.logD
import com.darcy.kmpdemo.ui.base.BaseViewModel
import com.darcy.kmpdemo.ui.base.IIntent
import com.darcy.kmpdemo.ui.base.IReducer
import com.darcy.kmpdemo.ui.base.impl.fetch.FetchIntent
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenState
import com.darcy.kmpdemo.ui.base.impl.paging.PageSize
import com.darcy.kmpdemo.ui.base.impl.paging.PagingIntent
import com.darcy.kmpdemo.ui.base.impl.screenstatus.ScreenStateIntent
import com.darcy.kmpdemo.ui.base.impl.tips.TipsIntent
import com.darcy.kmpdemo.ui.screen.learn.loaddata.reducer.LoadDataReducer
import com.darcy.kmpdemo.ui.screen.learn.loaddata.state.LoadDataState
import darcykmp_im.composeapp.generated.resources.Res
import darcykmp_im.composeapp.generated.resources.confirm
import darcykmp_im.composeapp.generated.resources.tips_success
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.getString
import kotlin.reflect.KClass

class LoadDataViewModel : BaseViewModel<LoadDataState>() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: KClass<T>,
                extras: CreationExtras
            ): T {
                return LoadDataViewModel() as T
            }
        }
    }

    override fun initState(): LoadDataState {
        return LoadDataState()
    }

    override fun initReducers(): List<IReducer<LoadDataState>> {
        return listOf(
            LoadDataReducer(),
        )
    }

    override fun dispatch(intent: IIntent) {
        logD("dispatch: $intent")
        when (intent) {
            is FetchIntent.ActionFetchData -> {
                actionLoadData()
            }

            is PagingIntent.ActionLoadNewPage -> {
                actionLoadNewPage(intent.pageNumber)
            }

            else -> {
                super.dispatch(intent)
            }
        }
    }

    private fun actionLoadNewPage(pageNumber: Int) {
        io {
            dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Loading))
            val pageSize: PageSize = _uiState.value.pagingState.currentPageSize
            delay(1_000)
            dispatch(
                PagingIntent.RefreshByLoadNewPage(
                    pageNumber,
                    LoadDataResponse(age = pageNumber, name = "darcy")
                )
            )
            dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Success))
        }
    }

    private fun actionLoadData() {
        io {
            dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Loading))
            delay(2_000)
            dispatch(FetchIntent.RefreshByFetchData(LoadDataResponse(age = 0, name = "darcy 加载数据成功")))
            dispatch(ScreenStateIntent.ScreenStateChange(ScreenState.Success))
            dispatch(TipsIntent.ShowTips(
                title = getString(Res.string.tips_success),
                tips = "页面加载成功",
                code = 200,
                middleButtonText = getString(Res.string.confirm),
            ))
        }
    }
}