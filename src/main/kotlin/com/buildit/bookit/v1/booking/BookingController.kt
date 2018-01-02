package com.buildit.bookit.v1.booking

import com.buildit.bookit.auth.UserPrincipal
import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
import com.buildit.bookit.v1.booking.dto.interval
import com.buildit.bookit.v1.location.bookable.BookableRepository
import com.buildit.bookit.v1.location.bookable.InvalidBookable
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.dto.Location
import com.buildit.bookit.v1.user.UserService
import com.buildit.bookit.v1.user.dto.maskSubjectIfOtherUser
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiParam
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.threeten.extra.Interval
import springfox.documentation.annotations.ApiIgnore
import java.net.URI
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES
import javax.validation.Valid

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
open class InvalidBooking(message: String) : RuntimeException(message)

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class StartInPastException : InvalidBooking("Start must be in the future")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class EndBeforeStartException : InvalidBooking("End must be after Start")

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class BookingNotFound : RuntimeException("Booking not found")

@ResponseStatus(value = HttpStatus.CONFLICT)
class BookableNotAvailable : RuntimeException("Bookable is not available.  Please select another time")

/**
 * Endpoint to manage bookings
 */
@RestController
@RequestMapping("/v1/booking")
@Transactional
class BookingController(private val bookingRepository: BookingRepository,
                        private val bookableRepository: BookableRepository,
                        private val userService: UserService,
                        private val clock: Clock
) {
    @GetMapping
    @Transactional(readOnly = true)
    fun getAllBookings(
        @AuthenticationPrincipal user: UserPrincipal?,
        @RequestParam("start", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm[[:ss][.SSS]]]")
        startDateInclusive: LocalDate? = null,
        @RequestParam("end", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm[[:ss][.SSS]]]")
        endDateExclusive: LocalDate? = null
    ): Collection<Booking> {
        val start = startDateInclusive ?: LocalDate.MIN
        val end = endDateExclusive ?: LocalDate.MAX

        if (!start.isBefore(end)) {
            throw EndBeforeStartException()
        }

        val allBookings = bookingRepository.findAll().toList()
        if (start == LocalDate.MIN && end == LocalDate.MAX)
            return allBookings.map { maskSubjectIfOtherUser(it, user) }

        return allBookings
            .filter { booking ->
                val timezone = booking.bookable.location.timeZone
                val desiredInterval = Interval.of(
                    start.atStartOfDay(timezone).toInstant(),
                    end.atStartOfDay(timezone).toInstant()
                )
                desiredInterval.overlaps(booking.interval(timezone))
            }
            .map { maskSubjectIfOtherUser(it, user) }
    }

    @GetMapping("/{id}")
    // use implicit params until springfox 2.8.0 released
    // https://github.com/springfox/springfox/commit/1606caf4a421470ccb9b7592b465979dcb4c5ce1
    // https://github.com/springfox/springfox/issues/2107
    // current workaround below from https://github.com/springfox/springfox/issues/2053
    @ApiImplicitParam(name = "id", required = true, dataTypeClass = String::class, paramType = "path")
    @Transactional(readOnly = true)
    fun getBooking(@PathVariable("id") @ApiParam(type = "java.lang.String") @ApiIgnore booking: Booking?, @AuthenticationPrincipal user: UserPrincipal?): Booking =
        booking?.let { maskSubjectIfOtherUser(it, user) } ?: throw BookingNotFound()

    @DeleteMapping("/{id}")
    // use implicit params until springfox 2.8.0 released
    // https://github.com/springfox/springfox/commit/1606caf4a421470ccb9b7592b465979dcb4c5ce1
    // https://github.com/springfox/springfox/issues/2107
    // current workaround below from https://github.com/springfox/springfox/issues/2053
    @ApiImplicitParam(name = "id", required = true, dataTypeClass = String::class, paramType = "path")
    fun deleteBooking(@PathVariable("id") @ApiParam(type = "java.lang.String") @ApiIgnore booking: Booking?, @AuthenticationPrincipal userPrincipal: UserPrincipal): ResponseEntity<Unit> =
        when {
            booking == null -> ResponseEntity.noContent().build()
            booking.user.externalId != userPrincipal.subject -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            else -> {
                bookingRepository.delete(booking)
                ResponseEntity.noContent().build()
            }
        }

    @Suppress("UnsafeCallOnNullableType")
    @PostMapping()
    fun createBooking(@Valid @RequestBody bookingRequest: BookingRequest, @AuthenticationPrincipal userPrincipal: UserPrincipal, errors: Errors? = null): ResponseEntity<Booking> {
        if (errors?.hasErrors() == true) {
            val errorMessage = errors.allErrors.joinToString(",", transform = { it.defaultMessage })

            throw InvalidBooking(errorMessage)
        }
        val bookable = bookableRepository.findOne(bookingRequest.bookableId) ?: throw InvalidBookable()

        val startDateTimeTruncated = bookingRequest.start!!.truncatedTo(MINUTES)
        val endDateTimeTruncated = bookingRequest.end!!.truncatedTo(MINUTES)

        validateBooking(bookable.location, startDateTimeTruncated, endDateTimeTruncated, bookable)

        val user = userService.register(userPrincipal)

        val booking = bookingRepository.save(
            Booking(
                bookable,
                bookingRequest.subject!!,
                startDateTimeTruncated,
                endDateTimeTruncated,
                user
            )
        )

        return ResponseEntity
            .created(URI("/v1/booking/${booking.id}"))
            .body(booking)
    }

    private fun validateBooking(location: Location, startDateTimeTruncated: LocalDateTime, endDateTimeTruncated: LocalDateTime, bookable: Bookable) {
        val now = LocalDateTime.now(clock.withZone(location.timeZone))
        if (!startDateTimeTruncated.isAfter(now)) {
            throw StartInPastException()
        }

        if (!endDateTimeTruncated.isAfter(startDateTimeTruncated)) {
            throw EndBeforeStartException()
        }

        val interval = Interval.of(
            startDateTimeTruncated.atZone(location.timeZone).toInstant(),
            endDateTimeTruncated.atZone(location.timeZone).toInstant()
        )

        val unavailable = bookingRepository.findByBookable(bookable)
            .any {
                interval.overlaps(
                    Interval.of(
                        it.start.atZone(location.timeZone).toInstant(),
                        it.end.atZone(location.timeZone).toInstant()))
            }

        if (unavailable) {
            throw BookableNotAvailable()
        }
    }
}
