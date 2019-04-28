package me.sandbox.sql.streaming.model

case class CassandraEventSourcing(agentName: String, period: Long, reason: String)
