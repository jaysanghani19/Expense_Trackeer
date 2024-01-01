package com.jay.expensetracker.addactivity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.jay.expensetracker.DatabaseClass
import com.jay.expensetracker.ImageConvertor
import com.jay.expensetracker.R
import com.jay.expensetracker.customlayout.ProgressBarDialog
import com.jay.expensetracker.dashboardacitivity.DashboardActivity
import com.jay.expensetracker.dashboardacitivity.dashboard.Dashboard
import com.jay.expensetracker.databinding.AddActivityBinding
import com.jay.expensetracker.dataclass.BalanceDataClass
import com.jay.expensetracker.dataclass.EntryDataClass
import com.jay.expensetracker.dataclass.NoOfEntriesDataClass
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabase
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseBuilder
import com.jay.expensetracker.entryroomdatabase.EntryRoomDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date


@Suppress("DEPRECATION")
class AddActivity : AppCompatActivity() {
    //    DataBinding of add_activity layout
    private lateinit var binding: AddActivityBinding

    //    DatePicker for choosing date of transaction
    private lateinit var datePicker: DatePickerDialog

    // ProgressBar dialog
    private lateinit var progressBarDialog: ProgressBarDialog

    //    To store Base64 version of the image
    private var chosenImageBase64Code: String? = null
    private var chosenImageLocation: String? = null

    //    Reference of the Firebase Database of Entries Child
    private lateinit var userDatabaseReference: DatabaseReference

    //    Date for the date textview
    private lateinit var date: String

    //    For category chosen by user
    var selectedCategory: String = ""

    //    ViewModel and dao
    private lateinit var viewModel: AddViewModel
    private lateinit var dao: EntryRoomDatabaseDao

    //    SharedPreference for the offline data(Current Balance)
    private lateinit var sharedPreferences: SharedPreferences

    private var isDebitEntry = false
    private var isCreditEntry = false

    //    DateFormat for setting date on date TextView
    @SuppressLint("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd")

    private var isAddingEntry = false

    private var isUpdating = false
    private var oldEntry = EntryRoomDatabase(0, "", "", 0f, "", "", "", "")


    companion object {
        //    item to show on dropdown menu of selectedCategory
        val categoryDebit = arrayOf(
            "Groceries", "Transportation", "Utilities", "Insurance",
            "Saving", "Entertainment", "Miscellaneous"
        )

        val categoryCredit = arrayOf(
            "Salary",
            "Bonus", "Insurance Claim", "Rental Income", "Profit", "Other"
        )

        //       static variable for the code of receiving image from user
        private const val BILL_IMAGE_CODE = 1
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.add_activity)

        progressBarDialog = ProgressBarDialog()
        dao = EntryRoomDatabaseBuilder.getInstance(this).getEntryDatabaseDao()
        val viewModelProvider = AddViewModelProvider(dao)
        viewModel = ViewModelProvider(this, viewModelProvider)[AddViewModel::class.java]


        DatabaseClass.setUserUID()

        userDatabaseReference = DatabaseClass.getInstanceOfEntriesDatabase()
            .getReference(DatabaseClass.USER_DATABASE_PATH)

        sharedPreferences =
            getSharedPreferences(Dashboard.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        val intent = intent
        if (intent != null) {
            val entryNumber = intent.getIntExtra(Dashboard.ENTRY_NUMBER, 0)
            if (entryNumber == 0) {
                //        Setting up views and clicklistener
                clickListenerOfDebit()
                setTodaysDate()
            } else {
                setEntry(entryNumber)
            }
        } else {
//        Setting up views and clicklistener
            clickListenerOfDebit()
            setTodaysDate()
        }
//        Setting up views and clicklistener
        setupChooseImage()
        setupClickListeners()
    }

    private fun getEntryOnEntryNumber(entryNumber: Int): EntryRoomDatabase {
        return viewModel.getEntryWithEntryNumber(entryNumber)
    }

    private fun setEntry(entryNumber: Int) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val oldEntry = getEntryOnEntryNumber(entryNumber)
                withContext(Dispatchers.Main) {
                    updateUIForOldEntry(oldEntry)
                }
            }
        }

    }

    private fun updateUIForOldEntry(oldEntry: EntryRoomDatabase) {
        this.oldEntry = oldEntry
        selectedCategory = oldEntry.category
        binding.btnSave.text = "Update"
        isUpdating = true
        binding.addAmount.text = SpannableStringBuilder(oldEntry.amount.toString())
        binding.dropdownMenuCategory.text = SpannableStringBuilder(oldEntry.category)
        binding.note.text = SpannableStringBuilder(oldEntry.note)
        binding.date.text = SpannableStringBuilder(oldEntry.date)
        if (oldEntry.transactionType == EntryRoomDatabase.CREDIT_TRANSACTION) {
            binding.debitButton.isClickable = false
            binding.debitButton.isContextClickable = false
            binding.debitButton.isLongClickable = false
            clickListenerOfCredit()
        } else {
            binding.creditButton.isClickable = false
            binding.creditButton.isContextClickable = false
            binding.creditButton.isLongClickable = false
            clickListenerOfDebit()
        }
        try {
            val bitMap = BitmapFactory.decodeFile(oldEntry.imagePath)
            binding.chosenImage.setImageBitmap(bitMap)
            binding.chosenImage.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this@AddActivity, "The Bill Image is Deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clickListenerOfDebit() {
        if (!isDebitEntry) {
            binding.debitButton.background = getDrawable(R.drawable.circular_50_counter_white_blue)
            binding.debitButton.setTextColor(getColor(R.color.white))
            binding.creditButton.background = getDrawable(R.drawable.circular_background)
            binding.creditButton.setTextColor(getColor(R.color.black))
            binding.title.text = EntryRoomDatabase.DEBIT_TRANSACTION
            val arrayAdapter = ArrayAdapter(
                this,
                androidx.transition.R.layout.support_simple_spinner_dropdown_item,
                categoryDebit
            )
            setupDropdownMenu(arrayAdapter)
            isDebitEntry = true
            isCreditEntry = false
        }
    }

    private fun clickListenerOfCredit() {
        if (!isCreditEntry) {
            binding.creditButton.background = getDrawable(R.drawable.circular_50_counter_white_blue)
            binding.creditButton.setTextColor(getColor(R.color.white))
            binding.debitButton.background = getDrawable(R.drawable.circular_background)
            binding.debitButton.setTextColor(getColor(R.color.black))
            binding.title.text = EntryRoomDatabase.CREDIT_TRANSACTION
            val arrayAdapter = ArrayAdapter(
                this,
                androidx.transition.R.layout.support_simple_spinner_dropdown_item,
                categoryCredit
            )
            setupDropdownMenu(arrayAdapter)
            isCreditEntry = true
            isDebitEntry = false
        }
    }

    //   this function will Set dropdownmenu for category and also adding addTextChangedListener
    private fun setupDropdownMenu(arrayAdapter: ArrayAdapter<String>) {
        binding.dropdownMenuCategory.setAdapter(arrayAdapter)

        binding.dropdownMenuCategory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.dropdownMenuCategory.clearFocus()
                selectedCategory = binding.dropdownMenuCategory.text.toString()
            }
        })
    }

    //    this function will Set up choosing image for bill
//    it'll create intent that open gallary for choosing the image
    private fun setupChooseImage() {
        binding.chooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, BILL_IMAGE_CODE)
        }
    }

    //    it has all clickListeners of views
    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            showProgressBarDialog()
            clickListenerOfBtnSave()
        }

        binding.date.setOnClickListener {
            clickListenerOfDate()
        }
        binding.debitButton.setOnClickListener {
            clickListenerOfDebit()
        }
        binding.creditButton.setOnClickListener {
            clickListenerOfCredit()
        }
    }

    //    this function will Set today's date in date EditText
    private fun setTodaysDate() {
        val date = Date()
        this.date = dateFormat.format(date)
        binding.date.text = this.date
    }

//    ClickListener of the views in layout

    //       this function will create an object of the DatePickerDialog
    //       after that user can choose the date,and setting date on date TextView
    private fun clickListenerOfDate() {
        datePicker = DatePickerDialog(this)
        datePicker.show()
        datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->

            var tempMonth: String = (month + 1).toString()
            var tempDay: String = dayOfMonth.toString()
            if (tempDay.length == 1) {
                tempDay = ("0$tempDay").toString()
            }
            if (tempMonth.length == 1) {
                tempMonth = "0$tempMonth"
            }
            this.date = "$year/$tempMonth/$tempDay"
            binding.date.text = this.date
        }
    }

    //    This function will Check every field is filled if not then we'll show the error
    //    and if every field is filled than we'll add entries into database using addEntry() function
    private fun clickListenerOfBtnSave() {
        val amount = binding.addAmount.text.toString()
        val date = binding.date.text.toString()
        val note = binding.note.text.toString()

        if (!DatabaseClass.isInternetAvailable(this)) {
            Toast.makeText(this, "Connect the Internet", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(amount)) {
            dismissProgressDialog()
            binding.addAmount.error = "Enter the Amount"
            binding.addAmount.requestFocus()
        } else if (TextUtils.isEmpty(date)) {
            dismissProgressDialog()
            binding.date.error = "Choose the date"
            binding.date.requestFocus()
        } else if (selectedCategory == "") {
            dismissProgressDialog()
            binding.dropdownMenuCategory.error = "Select the Category of oldEntry"
            binding.dropdownMenuCategory.requestFocus()
        } else if (TextUtils.isEmpty(note)) {
            dismissProgressDialog()
            binding.note.error = "Enter the note"
            binding.note.requestFocus()
        } else if (amount.toFloat() == 0.0f) {
            dismissProgressDialog()
            binding.addAmount.requestFocus()
            binding.addAmount.error = "Amount should not be 0"
        }
        //        Checking that user has chose the image or not
        else {
            if (!isUpdating) {
                if (chosenImageLocation == null) {
                    dismissProgressDialog()
                    binding.chooseImage.error = "Choose the image for the bill"
                } else {
                    getCurrentBalance(
                        amount.toFloat(),
                        date,
                        selectedCategory,
                        note,
                        chosenImageBase64Code!!
                    )
                }
            } else {
                updateEntry(
                    amount.toFloat(),
                    date,
                    selectedCategory,
                    note
                )
            }
        }
    }

    private fun updateEntry(amount: Float, date: String, selectedCategory: String, note: String) {
        val map = HashMap<String, Any>()
        map[EntryDataClass.AMOUNT] = amount
        map[EntryDataClass.CATEGORY] = selectedCategory
        map[EntryDataClass.DATE] = date
        map[EntryDataClass.NOTE] = note
        if (chosenImageBase64Code == null && chosenImageLocation == null) {
            val offlineEntry = EntryRoomDatabase(
                oldEntry.entryNumber,
                oldEntry.transactionType,
                oldEntry.timeStamp,
                amount,
                date,
                selectedCategory,
                note,
                oldEntry.imagePath
            )
            updateEntriesOnlineAndOffline(offlineEntry, map)
        } else {
            val offlineEntry = chosenImageLocation?.let {
                EntryRoomDatabase(
                    oldEntry.entryNumber,
                    oldEntry.transactionType,
                    oldEntry.timeStamp,
                    amount,
                    date,
                    selectedCategory,
                    note,
                    it
                )

            }
            chosenImageBase64Code?.let {
                map.put(EntryDataClass.BILLIMAGEBASE64CODE, it)
            }
            offlineEntry?.let {
                updateEntriesOnlineAndOffline(offlineEntry, map)
            }
        }
    }

    private fun updateEntriesOnlineAndOffline(
        offlineEntry: EntryRoomDatabase,
        map: HashMap<String, Any>,
    ) {
        val databaseRef = DatabaseClass.getInstanceOfEntriesDatabase()
            .getReference(DatabaseClass.USERS_ENTRYDATABASE_PATH())
            .child(offlineEntry.timeStamp)
        if (DatabaseClass.isInternetAvailable(this)) {
            databaseRef.updateChildren(map as Map<String, Any>).addOnCompleteListener {
                if (it.isSuccessful) {
                    viewModel.updateEntry(offlineEntry)
                    setUpdatedOfflineOnlineBalance(offlineEntry.transactionType, offlineEntry)
                    dismissProgressDialog()
                    Toast.makeText(this, "Entry is Updated", Toast.LENGTH_SHORT).show()
                    startDashboardActivity()
                } else {
                    dismissProgressDialog()
                    Toast.makeText(this, "Entry is not updated \n Try Again", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            dismissProgressDialog()
            Toast.makeText(this, "Connect to the Internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpdatedOfflineOnlineBalance(
        transactionType: String,
        updatedEntry: EntryRoomDatabase,
    ) {
        val databaseRef =
            DatabaseClass.getInstanceOfUserDatabase().getReference(DatabaseClass.USER_DATABASE_PATH)

        databaseRef.child(DatabaseClass.BALANCE_OF_USER)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val offlineBalance = sharedPreferences.getFloat(Dashboard.CURRENT_BALANCE, 0f)
                    val data = snapshot.getValue(BalanceDataClass::class.java)
                    val onlineBalance = data?.currentBalance
                    val diffrence = if (transactionType == EntryRoomDatabase.CREDIT_TRANSACTION) {
                        updatedEntry.amount - oldEntry.amount
                    } else {
                        oldEntry.amount - updatedEntry.amount
                    }
                    onlineBalance?.let {
                        offlineBalance.let { it2 ->
                            updateOnlineBalance(it.plus(diffrence)) { it3 ->
                                handleUpdateBalance(it3, it.plus(diffrence), it2.plus(diffrence))
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    entryNotAdded()
                }
            })
    }

    private fun getCurrentBalance(
        amount: Float,
        date: String,
        category: String,
        note: String,
        chosenImageBase64: String,
    ) {
        val databaseRef =
            DatabaseClass.getInstanceOfUserDatabase().getReference(DatabaseClass.USER_DATABASE_PATH)

        databaseRef.child(DatabaseClass.BALANCE_OF_USER)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val offlineBalance = sharedPreferences.getFloat(Dashboard.CURRENT_BALANCE, 0f)
                    if (snapshot.exists()) {
                        val data = snapshot.getValue(BalanceDataClass::class.java)
                        val onlineBalance = data?.currentBalance
                        if (onlineBalance != null) {
                            addEntry(
                                amount,
                                date,
                                category,
                                note,
                                chosenImageBase64,
                                onlineBalance,
                                offlineBalance
                            )
                        }
                    } else {
                        addEntry(
                            amount,
                            date,
                            category,
                            note,
                            chosenImageBase64,
                            0f,
                            offlineBalance
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    entryNotAdded()
                }
            })

    }

    //    this function will add the oldEntry based on the parameter
    //    function will get the noOfEntries From firebase database and invoke handleNoOfEntriesSnapShot() with user details
    private fun addEntry(
        amount: Float,
        date: String,
        category: String,
        note: String,
        chosenImageBase64: String,
        onlineBalance: Float,
        offlineBalance: Float,
    ) {
        userDatabaseReference =
            DatabaseClass.getInstanceOfUserDatabase().getReference(DatabaseClass.USER_DATABASE_PATH)

        userDatabaseReference.child(DatabaseClass.NO_OF_ENTRIES)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    handleNoOfEntriesSnapshot(
                        snapshot,
                        amount,
                        date,
                        category,
                        note,
                        chosenImageBase64,
                        onlineBalance,
                        offlineBalance
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    entryNotAdded()
                }
            })
    }

    //    This function will take snapshot of noOfEntries and create newEntryNumber and invoke addEntryToFirebaseAndRoomDatabase()
    private fun handleNoOfEntriesSnapshot(
        snapshot: DataSnapshot,
        amount: Float,
        date: String,
        category: String,
        note: String,
        chosenImageBase64: String,
        onlineBalance: Float,
        offlineBalance: Float,
    ) {
        val data = snapshot.getValue(NoOfEntriesDataClass::class.java)
        val totalEntries = data?.totalEntries ?: 0
        val newEntryNumber = totalEntries + 1
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val tempDate = Date()
        val timeStamp = sdf.format(tempDate)
        val creditOrDebit = if (isDebitEntry && !isCreditEntry) EntryRoomDatabase.DEBIT_TRANSACTION
        else EntryRoomDatabase.CREDIT_TRANSACTION
        val entryDataClass = EntryDataClass(
            newEntryNumber,
            creditOrDebit,
            timeStamp,
            amount,
            date,
            category,
            note,
            chosenImageBase64
        )

        addEntryToFirebaseAndRoomDatabase(
            newEntryNumber,
            entryDataClass,
            totalEntries,
            onlineBalance,
            offlineBalance
        )
    }

    //  This function will take newEntryNumber , entryDataClass and totalEntries
//  This function will add the oldEntry to the Firebase database and entryRoomDatabase and update the totalEntries,Current Balance in Firebase Database and SharedPreferences
    private fun addEntryToFirebaseAndRoomDatabase(
        newEntryNumber: Int,
        entryDataClass: EntryDataClass,
        totalEntries: Int,
        onlineBalance: Float,
        offlineBalance: Float,
    ) {
        val entry = chosenImageLocation?.let {
            entryDataClass.amount?.let { it1 ->
                entryDataClass.entryNumber?.let { it2 ->
                    EntryRoomDatabase(
                        it2,
                        entryDataClass.transactionType,
                        entryDataClass.timeStamp,
                        it1,
                        entryDataClass.date,
                        entryDataClass.category,
                        entryDataClass.note,
                        it
                    )
                }
            }
        }
        val updatedOnlineBalance =
            if (entryDataClass.transactionType == EntryRoomDatabase.CREDIT_TRANSACTION) {
                entryDataClass.amount?.let {
                    onlineBalance.plus(it)
                }
            } else {
                entryDataClass.amount?.let {
                    onlineBalance.minus(it)
                }
            }
        val updatedOfflineBalance =
            if (entryDataClass.transactionType == EntryRoomDatabase.CREDIT_TRANSACTION) {
                entryDataClass.amount?.let {
                    offlineBalance.plus(it)
                }
            } else {
                entryDataClass.amount?.let {
                    offlineBalance.minus(it)
                }
            }
        userDatabaseReference.child(DatabaseClass.ENTRIES)
            .child(entryDataClass.timeStamp)
            .setValue(entryDataClass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Adding in the Room Database
                    if (entry != null) {
                        viewModel.insertData(entry)
                    }
                    // Update the total entries count
                    // And if that succedeed than we will update the Current Balance in Firebase Database and SharedPreferences
                    setEntryNumber(totalEntries + 1) { success ->
                        if (success) {
                            updatedOfflineBalance?.let {
                                updatedOnlineBalance?.let { it2 ->
//                                    Updating Online Balance and checking it's success or not
                                    updateOnlineBalance(it2) { it3 ->
                                        handleUpdateBalance(
                                            it3,
                                            it2,
                                            it
                                        )
                                    }
                                }
                            }
                        }
                        handleEntryAdditionResult(success, totalEntries + 1)
                    }
                } else {
                    entryNotAdded()
                }
            }
    }

    //    This function will see the result if the balance is updated or not
//    if balance is not updated it'll try again
    private fun handleUpdateBalance(boolean: Boolean, onlineBalance: Float, offlineBalance: Float) {
        if (!boolean) {
            updateOnlineBalance(onlineBalance) {
                if (!it) handleUpdateBalance(false, onlineBalance, offlineBalance)
            }
            updateOfflineBalance(offlineBalance)
        } else {
            updateOfflineBalance(offlineBalance)
        }
    }

    //  This function will take newEntryNumber and update to the firebase database
    private fun handleEntryAdditionResult(success: Boolean, newEntryNumber: Int) {
        if (success) {
            dismissProgressDialog()
            Toast.makeText(this@AddActivity, "Entry is added", Toast.LENGTH_SHORT).show()
            startDashboardActivity()
        } else {
            userDatabaseReference.child(DatabaseClass.ENTRIES)
                .child(newEntryNumber.toString())
                .removeValue()
            viewModel.deleteEntry(newEntryNumber)
            entryNotAdded()
        }
    }

    //    this function will some UI changes if oldEntry is Not Added
    private fun entryNotAdded() {
        dismissProgressDialog()
        Toast.makeText(
            this@AddActivity,
            "Entry hasn't added.\nTry again",
            Toast.LENGTH_SHORT
        ).show()
    }

    //    This function will update the (no of entries) in database using firebase database reference
    private fun setEntryNumber(number: Int, callback: (Boolean) -> Unit) {
        val noOfEntries = mapOf(
            NoOfEntriesDataClass.TOTAL_ENTRIES to number
        )
        val noOfEntryReference = userDatabaseReference.child(DatabaseClass.NO_OF_ENTRIES)
        noOfEntryReference.updateChildren(noOfEntries).addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    //    This function will update the Current Balance in Firebase Database
    private fun updateOnlineBalance(updatedBalance: Float, callback: (Boolean) -> Unit) {
        val updatedBalanceMap = mapOf(
            BalanceDataClass.CURRENT_BALANCE to updatedBalance
        )
        val databaseRef =
            DatabaseClass.getInstanceOfUserDatabase().getReference(DatabaseClass.USERS_BALANCE_PATH())
        databaseRef.updateChildren(updatedBalanceMap).addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    //  This Function will update balance in SharedPreference
    private fun updateOfflineBalance(updatedBalance: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat(Dashboard.CURRENT_BALANCE, updatedBalance)
        editor.apply()
    }

    private fun startDashboardActivity() {
        val intent = Intent(this@AddActivity, DashboardActivity::class.java)
        intent.putExtra("temp", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    //    This 2 function will show ProgressBar and remove it
    private fun showProgressBarDialog() {
        progressBarDialog.show(supportFragmentManager, "ProgressBar")
        isAddingEntry = true
    }

    private fun dismissProgressDialog() {
        if (isAddingEntry) {
            progressBarDialog.dismiss()
            isAddingEntry = false
        }
    }

    //    onBackPressed Function will ensure that even if user click back Button Adding Entry will be continued
    override fun onBackPressed() {
        if (isAddingEntry) {
            Toast.makeText(this, "Entry is being added", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    //  this function will fetch the image chosen by the user and
    //  if the data is not null than it'll generate Base64 code and the path of the image
    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == BILL_IMAGE_CODE) {
                if (data != null) {
                    binding.chosenImage.visibility = View.VISIBLE
                    binding.chosenImage.setImageURI(data.data)
                    val imageURI = data.data
                    val imageBitmap =
                        getBitmap(this.contentResolver, imageURI)

                    if (imageURI != null) {
                        //    Base64Code for firebase realtime database
                        chosenImageBase64Code = ImageConvertor.encodeImageToBase64(imageBitmap)
                        //    Image Location for the Room Database
                        val imageFormat =
                            ImageConvertor.getImageFormat(this, imageURI)
                        chosenImageLocation =
                            imageFormat.let { ImageConvertor.getUniqueImagePath(this, it) }
                        ImageConvertor.handleImage(this, imageURI, chosenImageLocation.toString())
                    }
                }
            }
        }
    }

}