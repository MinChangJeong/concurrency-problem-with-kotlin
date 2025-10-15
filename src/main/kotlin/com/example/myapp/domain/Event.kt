package com.example.myapp.domain

import jakarta.persistence.*

@Entity
class Event(
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var capacity: Int
) {
    constructor(): this("", 0)

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
