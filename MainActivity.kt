
package com.dulz.pc

import android.app.DownloadManager
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var root: FrameLayout
    private lateinit var desktop: LinearLayout
    private var overlay: View? = null
    private val prefs by lazy { getSharedPreferences("dulz", MODE_PRIVATE) }
    private val apps = listOf("BluStrak", "Chrome", "File Explorer", "Settings", "Search", "Game Store")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        buildDesktop()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (overlay != null) closeOverlay() else buildDesktop()
            }
        })
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun tv(text: String, size: Float = 15f): TextView = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(Color.WHITE)
        setPadding(dp(10), dp(8), dp(10), dp(8))
    }
    private fun button(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text; setTextColor(Color.WHITE); setBackgroundColor(Color.TRANSPARENT)
        setOnClickListener { action() }
    }

    private fun buildDesktop() {
        closeOverlay()
        root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(5,9,20))
        setContentView(root)

        desktop = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(28), dp(18), dp(78))
        }
        root.addView(desktop, FrameLayout.LayoutParams(-1,-1))

        val brand = tv("Dulz | STRM", 23f)
        brand.setTextColor(Color.rgb(32,217,255))
        desktop.addView(brand)

        val title = tv("Dulz PC Launcher 2026", 14f)
        title.setTextColor(Color.LTGRAY)
        desktop.addView(title)

        val grid = GridLayout(this).apply { columnCount = 3; rowCount = 2 }
        desktop.addView(grid, LinearLayout.LayoutParams(-1,0,1f))
        val icons = listOf("🎮\nBluStrak","🌐\nChrome","📁\nFile Explorer","⚙️\nSettings","🔍\nSearch","🛒\nGame Store")
        icons.forEach { label ->
            val b = button(label, { launch(label.substringAfter("\n")) }).apply { textSize=14f }
            grid.addView(b, GridLayout.LayoutParams().apply {
                width=0; height=dp(110); columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f)
                setMargins(dp(4),dp(4),dp(4),dp(4))
            })
        }

        val bar = LinearLayout(this).apply {
            setBackgroundColor(Color.rgb(12,20,34)); gravity=Gravity.CENTER_VERTICAL
        }
        root.addView(bar, FrameLayout.LayoutParams(-1,dp(62),Gravity.BOTTOM))
        bar.addView(button("☰ Start"){showStartMenu()}, LinearLayout.LayoutParams(dp(90),-1))
        val spacer = Space(this); bar.addView(spacer,LinearLayout.LayoutParams(0,1,1f))
        val clock = tv("",13f); clock.gravity=Gravity.CENTER
        bar.addView(clock,LinearLayout.LayoutParams(dp(150),-1))
        val timer = Timer()
        timer.scheduleAtFixedRate(object: TimerTask(){ override fun run(){ runOnUiThread{
            clock.text=SimpleDateFormat("HH:mm:ss\nEEE, dd MMM yyyy",Locale.getDefault()).format(Date())
        }}},0,1000)
    }

    private fun launch(name: String) {
        when(name) {
            "BluStrak","Game Store" -> showBluStrak()
            "Chrome" -> openChrome()
            "File Explorer" -> openExplorer()
            "Settings" -> showSettings()
            "Search" -> showSearch()
        }
    }

    private fun panel(): LinearLayout = LinearLayout(this).apply {
        orientation=LinearLayout.VERTICAL; setPadding(dp(20),dp(28),dp(20),dp(20))
        setBackgroundColor(Color.rgb(9,16,29))
    }

    private fun showStartMenu() {
        closeOverlay()
        val p=panel()
        p.addView(tv("Dulz PC",24f)); p.addView(tv("Dulz | STRM",13f))
        val input=EditText(this).apply{hint="Cari aplikasi"; setTextColor(Color.WHITE); setHintTextColor(Color.GRAY)}
        p.addView(input)
        val list=LinearLayout(this); list.orientation=LinearLayout.VERTICAL; p.addView(list)
        fun fill(q:String="") {
            list.removeAllViews()
            apps.filter{it.contains(q,true)}.forEach{a->list.addView(button(a){closeOverlay();launch(a)},LinearLayout.LayoutParams(-1,dp(48)))}
            if(list.childCount==0) list.addView(tv("Aplikasi tidak ditemukan"))
        }
        input.addTextChangedListener(object: android.text.TextWatcher{
            override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){}
            override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){fill(s.toString())}
            override fun afterTextChanged(s:android.text.Editable?){}
        })
        fill()
        p.addView(button("⚙ Settings"){closeOverlay();showSettings()})
        p.addView(button("⏻ Power"){showPowerDialog()})
        showOverlay(p)
    }

    private fun showSearch() {
        val p=panel(); p.addView(tv("Search",24f))
        val input=EditText(this).apply{hint="Cari aplikasi";setTextColor(Color.WHITE);setHintTextColor(Color.GRAY)}
        p.addView(input)
        val results=LinearLayout(this);results.orientation=LinearLayout.VERTICAL;p.addView(results)
        fun fill(q:String){results.removeAllViews();apps.filter{it.contains(q,true)}.forEach{a->results.addView(button(a){closeOverlay();launch(a)})}
            if(results.childCount==0)results.addView(tv("Aplikasi tidak ditemukan"))}
        input.addTextChangedListener(object:android.text.TextWatcher{
            override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){}
            override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){fill(s.toString())}
            override fun afterTextChanged(s:android.text.Editable?){}
        });fill("")
        showOverlay(p)
    }

    private fun showBluStrak() {
        val p=panel();p.addView(tv("🎮 BluStrak",26f));p.addView(tv("Game & App Store internal",14f))
        p.addView(tv("Belum ada katalog bawaan. Masukkan URL HTTPS/HTTP file yang Anda berhak mengunduh."))
        val url=EditText(this).apply{hint="https://contoh.com/file.apk";setTextColor(Color.WHITE);setHintTextColor(Color.GRAY)}
        p.addView(url)
        val status=tv("Status: siap",13f);p.addView(status)
        p.addView(button("Download"){download(url.text.toString(),status)})
        p.addView(button("Riwayat Download"){showHistory()})
        p.addView(button("← Desktop"){closeOverlay()})
        showOverlay(p)
    }

    private fun download(raw:String,status:TextView){
        val uri=try{Uri.parse(raw)}catch(_:Exception){null}
        if(uri==null || (uri.scheme!="http" && uri.scheme!="https") || uri.host.isNullOrBlank()){
            status.text="Status: URL HTTP/HTTPS tidak valid";return
        }
        val req=DownloadManager.Request(uri).setTitle("Dulz PC Launcher")
            .setDescription("BluStrak download").setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.lastPathSegment?.takeIf{it.isNotBlank()} ?: "dulz-download")
        val dm=getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val id=dm.enqueue(req);prefs.edit().putLong("last_download",id).apply();status.text="Status: download dimulai (#$id)"
    }

    private fun showHistory(){
        val p=panel();p.addView(tv("Riwayat Download",24f))
        val id=prefs.getLong("last_download",-1)
        if(id<0)p.addView(tv("Belum ada download dari BluStrak."))
        else p.addView(tv("Download terakhir: #$id\nStatus dapat dilihat pada notifikasi Android."))
        p.addView(button("← Kembali"){closeOverlay();showBluStrak()});showOverlay(p)
    }

    private fun openChrome(){
        val pm=packageManager
        val intent=pm.getLaunchIntentForPackage("com.android.chrome")
        if(intent!=null)try{startActivity(intent)}catch(e:Exception){Toast.makeText(this,"Chrome tidak dapat dibuka.",Toast.LENGTH_SHORT).show()}
        else Toast.makeText(this,"Google Chrome tidak terinstall.",Toast.LENGTH_LONG).show()
    }

    private fun openExplorer(){
        val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{
            addCategory(Intent.CATEGORY_OPENABLE);type="*/*"
        }
        try{startActivityForResult(intent,42)}catch(e:Exception){Toast.makeText(this,"File picker tidak tersedia.",Toast.LENGTH_SHORT).show()}
    }

    @Deprecated("Handled for broad API 26 compatibility")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==42 && resultCode==RESULT_OK){
            data?.data?.let{Toast.makeText(this,"File dipilih: ${it.lastPathSegment ?: it}",Toast.LENGTH_LONG).show()}
        }
    }

    private fun showSettings(){
        val p=panel();p.addView(tv("⚙ Settings",25f))
        val dark=prefs.getBoolean("dark",true)
        p.addView(button("Theme: ${if(dark)"Dark" else "Light"}"){prefs.edit().putBoolean("dark",!dark).apply();showSettings()})
        p.addView(button("Wallpaper"){Toast.makeText(this,"Wallpaper launcher mengikuti background aplikasi. Pilihan wallpaper lanjutan dapat ditambahkan.",Toast.LENGTH_SHORT).show()})
        p.addView(button("Taskbar: aktif"){Toast.makeText(this,"Taskbar aktif.",Toast.LENGTH_SHORT).show()})
        p.addView(button("Desktop icons: aktif"){Toast.makeText(this,"Ikon desktop aktif.",Toast.LENGTH_SHORT).show()})
        p.addView(button("Animation: ${if(prefs.getBoolean("anim",true))"aktif" else "mati"}"){prefs.edit().putBoolean("anim",!prefs.getBoolean("anim",true)).apply();showSettings()})
        p.addView(button("Sound: ${if(prefs.getBoolean("sound",true))"aktif" else "mati"}"){prefs.edit().putBoolean("sound",!prefs.getBoolean("sound",true)).apply();showSettings()})
        p.addView(tv("About Dulz PC Launcher\nDulz | STRM\nVersion 2026.1\nNative Kotlin • Android 8.0+"))
        p.addView(button("← Desktop"){closeOverlay()});showOverlay(p)
    }

    private fun showPowerDialog(){
        AlertDialog.Builder(this).setTitle("Power")
            .setItems(arrayOf("Buka pengaturan launcher default","Tutup menu")){
                d,w->if(w==0)try{startActivity(Intent(Settings.ACTION_HOME_SETTINGS))}catch(_:Exception){}
            }.show()
    }

    private fun showOverlay(content:View){
        val frame=FrameLayout(this).apply{setBackgroundColor(0xAA000000.toInt())}
        frame.addView(content,FrameLayout.LayoutParams(-1,-1))
        val lp=FrameLayout.LayoutParams(dp(360),-1,Gravity.START)
        root.addView(frame,lp);overlay=frame
        if(prefs.getBoolean("anim",true)){frame.alpha=0f;frame.animate().alpha(1f).setDuration(180).start()}
    }

    private fun closeOverlay(){overlay?.let{root.removeView(it)};overlay=null}
}
