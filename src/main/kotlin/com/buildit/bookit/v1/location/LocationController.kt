package com.buildit.bookit.v1.location

import com.buildit.bookit.v1.location.dto.Location
import com.buildit.bookit.v1.location.dto.LocationNotFound
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiParam
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore

// /v1/location/id/bookable/id/booking

/**
 * Location endpoint.  Locations contain bookables
 */
@RestController
@RequestMapping("/v1/location")
@Transactional
class LocationController(private val locationRepository: LocationRepository) {
    @Transactional(readOnly = true)
    @GetMapping
    fun getLocations(): Collection<Location> = locationRepository.findAll().toList()

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    // use implicit params until springfox 2.8.0 released
    // https://github.com/springfox/springfox/commit/1606caf4a421470ccb9b7592b465979dcb4c5ce1
    // https://github.com/springfox/springfox/issues/2107
    // current workaround below from https://github.com/springfox/springfox/issues/2053
    @ApiImplicitParam(name = "id", required = true, dataTypeClass = String::class, paramType = "path")
    fun getLocation(@PathVariable("id") @ApiParam(type = "java.lang.String") @ApiIgnore location: Location?): Location =
        location ?: throw LocationNotFound()
}
