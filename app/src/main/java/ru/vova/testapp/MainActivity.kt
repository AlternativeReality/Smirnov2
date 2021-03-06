package ru.vova.testapp


import android.content.Context
import android.net.ConnectivityManager
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.net.URL


private const val CHARACTER_DATA_API =
    "https://developerslife.ru/random?json=true"
private const val TAG = "myLogs"
class MainActivity : AppCompatActivity() {
    var position:Int = 0
    var lastPosition=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getPrev.isEnabled=false
        getPrev.isClickable=false
        if (amIConnected()) {
            loadData()
        } else{
            mImageView.setImageResource(R.drawable.ic_cloud_off_24px)
            ImageViewText.text = "нет подключения"
        }
        getNext.setOnClickListener {
            nextGif()
            pBar.isVisible=true
        }
        getPrev.setOnClickListener {
            position--
            showPrew()
            Log.v(TAG, position.toString())
        }
    }



    private fun nextGif() {
        if (!amIConnected()) {
            position++
            if (position <= lastPosition) {
                showNext()
            } else {
                if (position > lastPosition) position = lastPosition + 1
                mImageView.setImageResource(R.drawable.ic_cloud_off_24px)
                pBar.isVisible=false
                ImageViewText.text = "нет подключения"
                Log.v(TAG, position.toString())
            }
        }

        if (amIConnected()) {
            try {
                if (memList.isEmpty()) {
                    position = 0
                    loadData()
                } else {
                    position++
                    lastPosition = position
                    showNext()
                    Log.v(TAG, position.toString())
                }
            } catch (e: Exception) {
                //position--
                position = memList.size
                showNext()
//                    mImageView.setImageResource(R.drawable.ic_baseline_not_interested_24)
//                    ImageViewText.text = "ошибка загрузки drrr"
                Log.v(TAG, position.toString())
            }
        } else {
            mImageView.setImageResource(R.drawable.ic_cloud_off_24px)
            pBar.isVisible=false
            ImageViewText.text = "нет подключения"
            Log.v(TAG, position.toString())
        }
    }
    val memList = mutableListOf<Generator.DataApi>()
    val scope = CoroutineScope(Dispatchers.IO)
    val scope2 = CoroutineScope(Dispatchers.Main)
    private fun loadData() = scope.launch {

        val apiData = URL(CHARACTER_DATA_API).readText()
        var builder = Gson()
        var simpleData: Generator.DataApi =
            builder.fromJson<Generator.DataApi>(apiData, Generator.DataApi::class.java)

        memList.add(simpleData)

        Log.v(TAG,"plen" + simpleData.id)
        setGif(simpleData)

    }
    private fun setGif(obj: Generator.DataApi) = scope2.launch {
        if (obj.gifURL!= ""){
            Glide.with(applicationContext).asGif().load(obj.gifURL).listener(object :
                RequestListener<GifDrawable> {
                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    pBar.isVisible = false
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    pBar.isVisible = false
                    mImageView.setImageResource(R.drawable.ic_cloud_off_24px)
                    return false
                }
//                override fun onResourceReady(p0: Drawable?, p1: Any?, p2: Target<GifDrawable>?, p3: DataSource?, p4: Boolean): Boolean {
//                    Log.d(TAG, "OnResourceReady")
//                    //do something when picture already loaded
//                    return false
//                }
            }).into(mImageView)}
        else {
            mImageView.setImageResource(R.drawable.ic_baseline_not_interested_24)
            ImageViewText.text = "ошибка загрузки"
        }

        ImageViewText.text = obj.description
        ImageViewText.isVisible = true
        if (obj == memList.first()) {
            getPrev.isEnabled = false
            getPrev.isClickable = false
        } else {
            getPrev.isEnabled = true
            getPrev.isClickable = true
        }
    }
    private fun showNext() {

        if (position == memList.size){
            loadData()
        }
        else {
            var currentObj = memList[position]
            setGif(currentObj)
        }
    }
    private fun showPrew() {
//        position++
//      var currentObj = memList.get(memList.size-1-position)
//        if (currentObj == memList.first()) {
//            getPrev.isEnabled=false
//            getPrev.isClickable=false
//        }
//        setGif(currentObj)
        //position--
        var currentObj = memList[position]
        if (currentObj == memList.first()) {
            getPrev.isEnabled=false
            getPrev.isClickable=false
        }
        setGif(currentObj)
    }

    private fun amIConnected(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

}