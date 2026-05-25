package com.darcy.kmpdemo.ui.base

interface IUseCase<S,T> {
    suspend operator fun invoke(params: Map<String, String> = emptyMap(), bean: S): Result<T>
}