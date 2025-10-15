package com.example.myapp.domain

import jakarta.persistence.*

@Entity
class Reservation(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    var event: Event,
    @Column(nullable = false)
    var username: String
) {
    constructor(): this(Event("", 0), "")

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
