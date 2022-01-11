package pl.edu.wsiz.weatherapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import pl.edu.wsiz.weatherapp.databinding.FragmentFirstBinding
import pl.edu.wsiz.weatherapp.network.ApiInterface
import pl.edu.wsiz.weatherapp.network.ForecastDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class FirstFragment() : Fragment(), KodeinAware {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private val API_KEY = "31b0e4e921035c8148e5c6ceb7ef4cce"


    override val kodein = Kodein.lazy {

        bind() from singleton { ApiInterface() }

    }
    private val api: ApiInterface by instance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cityInput = view.findViewById<TextInputEditText>(R.id.city)
        cityInput.setOnEditorActionListener { _, keyCode, event ->
            if (((event?.action ?: -1) == KeyEvent.ACTION_DOWN)
                || keyCode == EditorInfo.IME_ACTION_DONE
            ) {
                getCurrentWeather(cityInput.text.toString())
                view.hideSoftInput()

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    fun View.hideSoftInput() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    fun getCurrentWeather(city: String) {
        val currentWeather = api.getCurrentWeather(city, API_KEY)
        var result: ForecastDTO? = null
        currentWeather.enqueue(object : Callback<ForecastDTO?> {
            override fun onResponse(call: Call<ForecastDTO?>, response: Response<ForecastDTO?>) {
                result = response.body()
                if (result == null) {
                    Toast.makeText(context, "Nie znaleziono miasta", Toast.LENGTH_LONG).show()
                } else {
                    populateDataOnScreen(result!!)
                }
            }

            override fun onFailure(call: Call<ForecastDTO?>, t: Throwable) {
                Log.d("apiCall", "onFailure: ${t.message}")
            }
        })
    }

    fun populateDataOnScreen(data: ForecastDTO) {
        binding.address.text = "${data.name}, ${data.sys.country}"
        binding.updatedAt.text = convertToDateTimeString(data.dt, PatternType.DATE_PATTERN)
        binding.status.text =
            data.weather[0].description.replaceFirstChar { c: Char -> c.uppercaseChar() }
        binding.temp.text = "${data.main.temp}°C"
        binding.tempMin.text = "Min temp: ${data.main.temp_min}°C"
        binding.tempMax.text = "Max temp: ${data.main.temp_max}°C"
        binding.sunrise.text = convertToDateTimeString(data.sys.sunrise, PatternType.TIME_PATTERN)
        binding.sunset.text = convertToDateTimeString(data.sys.sunset, PatternType.TIME_PATTERN)
        binding.wind.text = "${data.wind.speed}m/s"
        binding.pressure.text = "${data.main.pressure}hPa"
        binding.humidity.text = "${data.main.humidity}%"
        binding.feelsLike.text = "${data.main.feels_like}°C"


    }

    private fun convertToDateTimeString(field: Int, pattern: PatternType): String {
        return SimpleDateFormat(pattern.value).format(Date(field.toLong() * 1000))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}