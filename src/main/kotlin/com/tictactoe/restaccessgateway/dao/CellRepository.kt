package com.tictactoe.restaccessgateway.dao

import com.tictactoe.restaccessgateway.domain.CellStore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
interface CellRepository: CrudRepository<CellStore, String>