package com.openclassrooms.rebonnte.di

import com.openclassrooms.rebonnte.ui.medicine.MedicineRepository
import com.openclassrooms.rebonnte.ui.medicine.MedicineRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicineRepository(
        impl: MedicineRepository
    ): MedicineRepositoryInterface
}